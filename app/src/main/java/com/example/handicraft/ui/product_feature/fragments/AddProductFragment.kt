package com.example.handicraft.ui.product_feature.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentAddProductBinding
import com.example.handicraft_graduation_project_2025.data.models.Product
import com.example.handicraft.ui.product_feature.adapters.ColorAdapter
import com.example.handicraft.ui.product_feature.adapters.OnColorsClickListener
import com.example.handicraft_graduation_project_2025.ui.product_feature.adapters.ProductImagePagerAdapter
import com.example.handicraft.ui.product_feature.viewmodels.ProductsViewModel
import com.example.handicraft_graduation_project_2025.utils.CloudinaryManager
import com.example.handicraft_graduation_project_2025.utils.CloudinaryManager.uriToFile
import com.example.handicraft.utils.Resource
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil
import com.google.android.material.snackbar.Snackbar
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.launch

class AddProductFragment : Fragment(), OnColorsClickListener {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProductsViewModel
    private lateinit var productImagesAdapter: ProductImagePagerAdapter
    private lateinit var colorsAdapter: ColorAdapter

    private val productImages = mutableListOf<Uri>()
    private val productColors = mutableListOf<Int>()
    private lateinit var currentUserID: String

    // region Lifecycle
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        CloudinaryManager.initialize(requireContext())
        currentUserID = SharedPrefUtil.getUid(requireContext()) ?: ""

        viewModel = ViewModelProvider(this)[ProductsViewModel::class.java]
        observeViewModel()

        setupDropdowns()
        setupImagePager()
        setupColorsRecyclerView()
        setupSubmitButton()
        addDefaultColors()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    // region Setup
    private fun setupDropdowns() {
        val materialTypes = resources.getStringArray(R.array.materials)
        val categories = resources.getStringArray(R.array.productCategories)

        binding.actvMaterialType.setAdapter(ArrayAdapter(requireContext(), R.layout.item_material, materialTypes))
        binding.actvCategory.setAdapter(ArrayAdapter(requireContext(), R.layout.item_material, categories))
    }

    private fun setupImagePager() {
        productImagesAdapter = ProductImagePagerAdapter(productImages)
        binding.viewPagerImages.adapter = productImagesAdapter

        binding.fabAddImage.setOnClickListener { openGallery() }

        binding.btnDeleteImage.setOnClickListener {
            val currentPosition = binding.viewPagerImages.currentItem
            if (currentPosition in productImages.indices) {
                productImages.removeAt(currentPosition)
                productImagesAdapter.notifyDataSetChanged()
                toggleDeleteButton()
            }
        }

        toggleDeleteButton()
    }

    private fun setupColorsRecyclerView() {
        colorsAdapter = ColorAdapter(productColors, this)
        binding.rvColors.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = colorsAdapter
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateInputs()) {
                val selectedSizes = mutableListOf<String>().apply {
                    if (binding.chipSmall.isChecked) add("Small")
                    if (binding.chipMedium.isChecked) add("Medium")
                    if (binding.chipLarge.isChecked) add("Large")
                }

                val product = Product(
                    userId = currentUserID,
                    title = binding.etProductTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    materialType = binding.actvMaterialType.text.toString(),
                    category = binding.actvCategory.text.toString(),
                    price = binding.etProductPrice.text.toString().toDouble(),
                    sizes = selectedSizes,
                    colors = productColors.filter { it != Color.TRANSPARENT },
                    imageUrls = emptyList()
                )

                saveProduct(product)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.addProductStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    Snackbar.make(binding.root, "Product added successfully!", Snackbar.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    Snackbar.make(binding.root, "Failed to add product: ${result.message}", Snackbar.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    // Optional loading logic
                }
            }
        }
    }
    // endregion

    // region Color Picker
    override fun onAddColorClicked() = showColorPicker()

    override fun onColorClicked(view: View, position: Int) = showColorContextMenu(position, view)

    private fun addDefaultColors() {
        productColors.apply {
            add(Color.parseColor("#7E4ECC"))
            add(Color.BLACK)
            add(Color.TRANSPARENT)
        }
        colorsAdapter.notifyDataSetChanged()
    }

    private fun showColorPicker() {
        ColorPickerDialog.Builder(requireContext())
            .setTitle("Choose Color")
            .setPositiveButton("Select", ColorEnvelopeListener() { envelope, _ ->
                val color = envelope.color
                if (productColors.isNotEmpty()) {
                    productColors.add(productColors.size - 1, color)
                } else {
                    productColors.add(color)
                    productColors.add(Color.TRANSPARENT)
                }
                colorsAdapter.notifyDataSetChanged()
            })
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .show()
    }

    private fun showColorContextMenu(position: Int, view: View) {
        val popup = PopupMenu(view.context, view, Gravity.NO_GRAVITY, 0, R.style.CustomPopupMenu)
        popup.menuInflater.inflate(R.menu.edit_delete_menu, popup.menu)

        // Force show icons in the popup menu
        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popup)
            mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Set text color for each menu item
        for (i in 0 until popup.menu.size()) {
            val menuItem = popup.menu.getItem(i)
            val span = SpannableString(menuItem.title)
            val editColor = ContextCompat.getColor(view.context, R.color.text_primary)
            val deleteColor = ContextCompat.getColor(view.context, R.color.text_delete)
            when (menuItem.itemId) {
                R.id.action_edit -> span.setSpan(ForegroundColorSpan(editColor), 0, span.length, 0)
                R.id.action_delete -> span.setSpan(ForegroundColorSpan(deleteColor), 0, span.length, 0)
            }
            menuItem.title = span
        }
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit -> {
                    showEditColorDialog(position)
                    true
                }
                R.id.action_delete -> {
                    productColors.removeAt(position)
                    colorsAdapter.updateColors(productColors)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showEditColorDialog(position: Int) {
        ColorPickerDialog.Builder(requireContext())
            .setTitle("Edit Color")
            .setPositiveButton("Select", ColorEnvelopeListener() { envelope, _ ->
                productColors[position] = envelope.color
                colorsAdapter.updateColors(productColors)
            })
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .show()
    }
    // endregion

    // region Gallery Image Selection
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        productImages.add(clipData.getItemAt(i).uri)
                    }
                } else {
                    data.data?.let { uri -> productImages.add(uri) }
                }
                productImagesAdapter.notifyDataSetChanged()
                toggleDeleteButton()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        galleryLauncher.launch(intent)
    }

    private fun toggleDeleteButton() {
        binding.btnDeleteImage.visibility = if (productImages.isNotEmpty()) View.VISIBLE else View.GONE
    }
    // endregion

    // region Validation & Upload
    private fun validateInputs(): Boolean {
        var isValid = true

        if (productImages.isEmpty()) {
            Snackbar.make(binding.root, "Please add at least one product image", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

        if (binding.etProductTitle.text.isNullOrBlank()) {
            binding.tilProductTitle.error = "Product title is required"
            isValid = false
        } else binding.tilProductTitle.error = null

        if (binding.etDescription.text.isNullOrBlank()) {
            binding.tilDescription.error = "Description is required"
            isValid = false
        } else binding.tilDescription.error = null

        if (!binding.chipSmall.isChecked && !binding.chipMedium.isChecked && !binding.chipLarge.isChecked) {
            Snackbar.make(binding.root, "Please select at least one size", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

        if (binding.actvMaterialType.text.isNullOrBlank()) {
            binding.tilMaterialType.error = "Material type is required"
            isValid = false
        } else binding.tilMaterialType.error = null

        if (productColors.none { it != Color.TRANSPARENT }) {
            Snackbar.make(binding.root, "Please add at least one color", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun saveProduct(product: Product) {
        lifecycleScope.launch {
            val imageUrls = mutableListOf<String>()

            for ((index, uri) in productImages.withIndex()) {
                try {
                    val imageFile = uriToFile(uri, requireContext())
                    val uploadResult = CloudinaryManager.uploadImage(
                        context = requireContext(),
                        imageFile = imageFile,
                        collectionPath = "images",
                        documentId = product.title + index,
                        imageField = "imageUrl"
                    )

                    uploadResult.onSuccess { url ->
                        imageUrls.add(url)
                    }.onFailure { e ->
                        Snackbar.make(binding.root, "فشل رفع صورة: ${e.message}", Snackbar.LENGTH_LONG).show()
                        return@launch
                    }

                } catch (e: Exception) {
                    Snackbar.make(binding.root, "خطأ أثناء تجهيز الصورة: ${e.message}", Snackbar.LENGTH_LONG).show()
                    return@launch
                }
            }

            product.imageUrls = imageUrls
            viewModel.addOrUpdateProduct(product)
        }
    }
    // endregion
}
