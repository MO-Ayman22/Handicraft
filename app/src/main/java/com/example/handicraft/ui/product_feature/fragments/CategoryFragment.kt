package com.example.handicraft_graduation_project_2025.ui.product_feature.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.handicraft.R
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.FragmentCategoryBinding
import com.example.handicraft.ui.product_feature.adapters.OnProductClickListener
import com.example.handicraft.ui.product_feature.fragments.ProductDetailsFragment
import com.example.handicraft.utils.Constants
import com.example.handicraft_graduation_project_2025.data.models.Product
import com.example.handicraft.ui.product_feature.adapters.ProductGridAdapter
import com.example.handicraft.ui.product_feature.viewmodels.CategoryViewModel
import com.example.handicraft.utils.Resource
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil

class CategoryFragment : Fragment(), OnProductClickListener {

    private lateinit var currentUser: User
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CategoryViewModel

    private lateinit var productsAdapter: ProductGridAdapter

    private var currentProductList: List<Product> = emptyList()
    private var usersMap: Map<String, User> = emptyMap()

    private var categoryKey: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryKey = arguments?.getString(Constants.CATEGORY_KEY).orEmpty()

        initViewModels()
        setupUI()
        observeViewModels()

        viewModel.fetchUserById(SharedPrefUtil.getUid(requireContext())!!)
        binding.backArrow.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initViewModels() {
        viewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
    }

    private fun setupUI() {
        setupRecyclerView()
        setupCategoryTitle()
    }

    private fun setupRecyclerView() {
        productsAdapter = ProductGridAdapter(emptyList(), this)
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupCategoryTitle() {
        val titleResId = when (categoryKey) {
            Constants.GIFTS_KEY -> R.string.gifts
            Constants.JEWELRY_KEY -> R.string.jewelry
            Constants.CLOTHES_KEY -> R.string.clothes
            Constants.DECORATIONS_KEY -> R.string.decoration
            Constants.PRINTS_KEY -> R.string.prints
            Constants.ACCESSORIES_KEY -> R.string.accessories
            else -> R.string.app_name
        }
        binding.categoryLabel.setText(titleResId)
    }

    private fun observeViewModels() {
        viewModel.user.observe(viewLifecycleOwner) {

            it?.let {
                currentUser = it
                viewModel.fetchProductsByCategory(categoryKey)
            }
        }
        viewModel.categoryProducts.observe(viewLifecycleOwner) { products ->
            when (products) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Resource.Success -> {
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    currentProductList = products.data
                    viewModel.fetchUsersByIds(products.data.map { it.userId })
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    currentProductList = emptyList()
                    usersMap = emptyMap()
                    updateProductList()
                }
            }
        }

        viewModel.users.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()){
                usersMap = it.associateBy { user -> user.uid }
                updateProductList()
            }
        }
    }

    private fun updateProductList() {
        productsAdapter.updateList(currentProductList, usersMap, currentUser)
        binding.noProductsLayout.visibility =
            if (currentProductList.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onProductClick(productId: String, position: Int) {
       findNavController().navigate(R.id.action_categoryFragment_to_productDetailsFragment, Bundle().apply {
           putString(Constants.PRODUCT_KEY, productId)
       })
    }

    override fun onFavouriteToggle(productId: String, newState: Boolean) {
        val currentUser = SharedPrefUtil.getUid(requireContext())!!
        if (newState)
            viewModel.addToFavorites(currentUser, productId)
        else
            viewModel.removeFromFavorites(currentUser, productId)
        productsAdapter.toggleFavouriteState(productId)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
