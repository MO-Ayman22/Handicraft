package com.example.handicraft.ui.product_feature.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.handicraft.R
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.DialogFiltrationBinding
import com.example.handicraft.databinding.FragmentProductsBinding
import com.example.handicraft.ui.product_feature.adapters.OnProductClickListener

import com.example.handicraft_graduation_project_2025.data.models.Product

import com.example.handicraft.ui.product_feature.adapters.ProductGridAdapter
import com.example.handicraft.ui.product_feature.viewmodels.ProductsViewModel
import com.example.handicraft.utils.Constants
import com.example.handicraft_graduation_project_2025.ui.product_feature.fragments.CategoryFragment
import com.example.handicraft_graduation_project_2025.utils.Resource
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil
import com.google.android.material.chip.Chip

class ProductsFragment : Fragment(), OnProductClickListener {

    private lateinit var binding: FragmentProductsBinding
    private lateinit var viewModel: ProductsViewModel
    private lateinit var productsAdapter: ProductGridAdapter
    private var currentProductList: List<Product> = emptyList()
    private var usersMap: Map<String, User> = emptyMap()
    private val selectedSizes = mutableListOf<String>()
    private val selectedMaterials = mutableListOf<String>()
    private val selectedCategories = mutableListOf<String>()
    private var craftsmanName = ""
    private lateinit var currentUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModels()
        setupRecyclerView()
        setupSearchBar()
        setupCategoryClickListeners()
        observeData()

        viewModel.fetchUserById(SharedPrefUtil.getUid(requireContext())!!)
    }

    private fun setupViewModels() {
        viewModel = ViewModelProvider(this)[ProductsViewModel::class.java]
    }

    private fun observeData() {
        viewModel.user.observe(viewLifecycleOwner) {
            it?.let {
                currentUser = it
                viewModel.fetchProductsWithFilter(
                    null,
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    null
                )
            }
        }
        viewModel.searchResults.observe(viewLifecycleOwner) { products ->
            when (products) {
                is Resource.Error -> {
                    currentProductList = emptyList()
                    usersMap = emptyMap()
                    updateUIIfReady()
                }

                Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Resource.Success -> {
                    currentProductList = products.data
                    viewModel.fetchUsersByIds(products.data.map { it.userId })
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
        viewModel.users.observe(viewLifecycleOwner) { users ->
            usersMap = users.associateBy { it.uid }
            updateUIIfReady()
        }
    }

    private fun updateUIIfReady() {
        productsAdapter.updateList(currentProductList, usersMap, currentUser)
        binding.noProductsLayout.visibility =
            if (currentProductList.isEmpty()) View.VISIBLE else View.GONE
    }


    private fun setupRecyclerView() {
        productsAdapter = ProductGridAdapter(emptyList(), this)
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchBar() {
        binding.ivFiltration.setOnClickListener { showFilterDialog() }

        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString()
            val filteredList =
                currentProductList.filter { it.title.contains(query, ignoreCase = true) }
            val titles = filteredList.map { it.title }.distinct().take(10)
            val displayTitles = titles.ifEmpty { listOf("No results found") }
            val adapter = ArrayAdapter(requireContext(), R.layout.item_material, displayTitles)
            binding.etSearch.setAdapter(adapter)
        }
        binding.etSearch.setOnItemClickListener { parent, _, position, _ ->
            val adapter = parent.adapter

            // Safely check if position is valid
            if (position in 0 until adapter.count) {
                val selectedItem = adapter.getItem(position) as? String
                if (selectedItem is String && selectedItem != "No results found") {
                    viewModel.fetchProductsWithFilter(
                        craftsmanName = craftsmanName,
                        selectedCategories = selectedCategories,
                        selectedSizes = selectedSizes,
                        selectedMaterials = selectedMaterials,
                        productTitleQuery = selectedItem
                    )
                }
            } else {
                // Log or handle unexpected case
                Log.w("Search", "Invalid item position clicked: $position")
            }
        }

    }

    private fun setupCategoryClickListeners() {
        val categoryMap = mapOf(
            binding.llGiftsCategory to Constants.GIFTS_KEY,
            binding.llJewelryCategory to Constants.JEWELRY_KEY,
            binding.llClothesCategory to Constants.CLOTHES_KEY,
            binding.llDecorationCategory to Constants.DECORATIONS_KEY,
            binding.llPrintsCategory to Constants.PRINTS_KEY,
            binding.llAccessoriesCategory to Constants.ACCESSORIES_KEY
        )

        categoryMap.forEach { (layout, key) ->
            layout.setOnClickListener {
                navigateTo(CategoryFragment.newInstance(key))
            }
        }
    }

    private fun showFilterDialog() {
        val dialogBinding = DialogFiltrationBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        dialogBinding.btnReset.setOnClickListener {
            dialogBinding.etCraftsman.setText("")
            clearChips(dialogBinding.cgCategory)
            clearChips(dialogBinding.cgSize)
            clearChips(dialogBinding.cgMaterial)
        }

        dialogBinding.btnSearch.setOnClickListener {
            selectedCategories.setFromChips(dialogBinding.cgCategory)
            selectedSizes.setFromChips(dialogBinding.cgSize)
            selectedMaterials.setFromChips(dialogBinding.cgMaterial)
            craftsmanName = dialogBinding.etCraftsman.text.toString().trim()
            viewModel.fetchProductsWithFilter(
                craftsmanName,
                selectedCategories,
                selectedSizes,
                selectedMaterials,
                null
            )
            binding.etSearch.setText("")
            Log.d(
                "TAG",
                "${selectedCategories} / ${selectedSizes} / ${selectedMaterials} / ${craftsmanName}"
            )
            dialog.dismiss()
        }

        dialog.setOnShowListener {
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        dialogBinding.cgCategory.checkSelectedChips(selectedCategories)
        dialogBinding.cgSize.checkSelectedChips(selectedSizes)
        dialogBinding.cgMaterial.checkSelectedChips(selectedMaterials)
        dialogBinding.etCraftsman.setText(craftsmanName)

        dialog.show()
    }

    private fun MutableList<String>.setFromChips(chipGroup: ViewGroup) {
        clear()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.isChecked) add(chip.text.toString())
        }
    }

    private fun ViewGroup.checkSelectedChips(selected: List<String>) {
        for (i in 0 until childCount) {
            val chip = getChildAt(i) as Chip
            chip.isChecked = selected.contains(chip.text.toString())
        }
    }

    private fun clearChips(chipGroup: ViewGroup) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.isChecked = false
        }
    }

    private fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
       /* requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            if (addToBackStack) addToBackStack(null)
            commit()
        }*/
    }

    override fun onProductClick(productId: String, position: Int) {
        navigateTo(ProductDetailsFragment.newInstance(productId))
    }

    override fun onFavouriteToggle(productId: String, newState: Boolean) {
        val currentUser = SharedPrefUtil.getUid(requireContext())!!
        if (newState)
            viewModel.addToFavorites(currentUser, productId)
        else
            viewModel.removeFromFavorites(currentUser, productId)
        productsAdapter.toggleFavouriteState(productId)
    }
}
