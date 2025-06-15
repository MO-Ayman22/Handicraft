package com.example.handicraft.ui.product_feature.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentAddProductBinding
import com.example.handicraft_graduation_project_2025.data.models.Product
import com.example.handicraft.ui.product_feature.adapters.ColorAdapter
import com.example.handicraft_graduation_project_2025.ui.product_feature.adapters.ProductImagePagerAdapter
import com.example.handicraft.ui.product_feature.viewmodels.ProductsViewModel
import com.example.handicraft_graduation_project_2025.utils.CloudinaryManager
import com.example.handicraft_graduation_project_2025.utils.CloudinaryManager.uriToFile
import com.example.handicraft_graduation_project_2025.utils.Resource
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil
import com.google.android.material.snackbar.Snackbar
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.launch

class AddProductFragment : Fragment() {
    lateinit var currentUserID: String
    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var productImagesAdapter: ProductImagePagerAdapter
    private lateinit var colorsAdapter: ColorAdapter
    private lateinit var viewModel: ProductsViewModel

    private val productImages = mutableListOf<Uri>()
    private val productColors = mutableListOf<Int>()


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
        viewModel.addProductStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    Snackbar.make(binding.root, "Product added successfully!", Snackbar.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
                is Resource.Error -> {
                    Snackbar.make(
                        binding.root,
                        "Failed to add product: ${result.message ?: "Unknown error"}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is Resource.Loading -> {
                    //  show
                    // Optional: Show loading indicator
                }
            }
        }

        setupMaterialTypeDropdown()
        setupProductCategoryDropdown()
        setupImageViewPager()
        setupColorsRecyclerView()
        setupSubmitButton()
        addDefaultColors()
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri
                        productImages.add(imageUri)
                    }
                } else {
                    data.data?.let { uri ->
                        productImages.add(uri)
                    }
                }
                productImagesAdapter.notifyDataSetChanged()
                binding.btnDeleteImage.visibility =
                    if (productImages.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupMaterialTypeDropdown() {
        val materialTypes = resources.getStringArray(R.array.materials)
        val adapter = ArrayAdapter(requireContext(), R.layout.item_material, materialTypes)
        binding.actvMaterialType.setAdapter(adapter)
    }

    private fun setupProductCategoryDropdown() {
        val productCategories = resources.getStringArray(R.array.productCategories)
        val adapter = ArrayAdapter(requireContext(), R.layout.item_material, productCategories)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun setupImageViewPager() {
        productImagesAdapter = ProductImagePagerAdapter(productImages)
        binding.viewPagerImages.adapter = productImagesAdapter

        binding.fabAddImage.setOnClickListener {
            openGallery()
        }

        binding.btnDeleteImage.setOnClickListener {
            val currentPosition = binding.viewPagerImages.currentItem
            if (productImages.isNotEmpty() && currentPosition < productImages.size) {
                productImages.removeAt(currentPosition)
                productImagesAdapter.notifyDataSetChanged()
                binding.btnDeleteImage.visibility =
                    if (productImages.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.btnDeleteImage.visibility = View.GONE
    }

    private fun setupColorsRecyclerView() {
        colorsAdapter = ColorAdapter(productColors)

        binding.rvColors.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = colorsAdapter
        }
    }

    private fun showColorContextMenu(position: Int, view: View) {
        val popupMenu = android.widget.PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.edit_delete_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
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

        popupMenu.show()
    }

    private fun showEditColorDialog(position: Int) {
        ColorPickerDialog.Builder(requireContext())
            .setTitle("Edit Color")
            .setPositiveButton("Select", ColorEnvelopeListener { envelope, _ ->
                productColors[position] = envelope.color
                colorsAdapter.updateColors(productColors)
            })
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .show()
    }


    private fun addDefaultColors() {
        productColors.add(Color.parseColor("#7E4ECC"))
        productColors.add(Color.parseColor("#000000"))
        productColors.add(Color.TRANSPARENT)
        colorsAdapter.notifyDataSetChanged()
    }

    private fun showColorPicker() {
        ColorPickerDialog.Builder(requireContext())
            .setTitle("Choose Color")
            .setPositiveButton("Select", ColorEnvelopeListener { envelope, _ ->
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

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateInputs()) {
                val selectedSizes = mutableListOf<String>()
                if (binding.chipSmall.isChecked) selectedSizes.add("Small")
                if (binding.chipMedium.isChecked) selectedSizes.add("Medium")
                if (binding.chipLarge.isChecked) selectedSizes.add("Large")

                val product = Product(
                    userId = currentUserID,
                    price = binding.etProductPrice.text.toString().toDouble(),
                    category = binding.actvCategory.text.toString(),
                    title = binding.etProductTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    sizes = selectedSizes,
                    materialType = binding.actvMaterialType.text.toString(),
                    imageUrls = emptyList(), // Replace later with uploaded image URLs
                    colors = productColors.filter { it != Color.TRANSPARENT }
                )
                saveProduct(product)
            }
        }
    }

    private fun openGallery() {
        val intent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        galleryLauncher.launch(intent)
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (productImages.isEmpty()) {
            Snackbar.make(
                binding.root,
                "Please add at least one product image",
                Snackbar.LENGTH_SHORT
            ).show()
            isValid = false
        }

        if (binding.etProductTitle.text.isNullOrBlank()) {
            binding.tilProductTitle.error = "Product title is required"
            isValid = false
        } else {
            binding.tilProductTitle.error = null
        }

        if (binding.etDescription.text.isNullOrBlank()) {
            binding.tilDescription.error = "Description is required"
            isValid = false
        } else {
            binding.tilDescription.error = null
        }

        if (!binding.chipSmall.isChecked && !binding.chipMedium.isChecked && !binding.chipLarge.isChecked) {
            Snackbar.make(binding.root, "Please select at least one size", Snackbar.LENGTH_SHORT)
                .show()
            isValid = false
        }

        if (binding.actvMaterialType.text.isNullOrBlank()) {
            binding.tilMaterialType.error = "Material type is required"
            isValid = false
        } else {
            binding.tilMaterialType.error = null
        }

        if (productColors.filter { it != Color.TRANSPARENT }.isEmpty()) {
            Snackbar.make(binding.root, "Please add at least one color", Snackbar.LENGTH_SHORT)
                .show()
            isValid = false
        }

        return isValid
    }

    private fun saveProduct(product: Product) {
        lifecycleScope.launch {
            val imageUrls = mutableListOf<String>()

            for ((index, uri) in productImages.withIndex()) {
                try {
                    val imageFile = uriToFile(uri,requireContext())
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
                        Snackbar.make(
                            binding.root,
                            "فشل رفع صورة: ${e.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                } catch (e: Exception) {
                    Snackbar.make(
                        binding.root,
                        "خطأ أثناء تجهيز الصورة: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                    return@launch
                }
            }


            product.imageUrls = imageUrls
            viewModel.addOrUpdateProduct(product)
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
