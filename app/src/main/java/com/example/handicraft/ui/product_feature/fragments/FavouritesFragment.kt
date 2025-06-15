package com.example.handicraft_graduation_project_2025.ui.product_feature.fragments

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handicraft.R
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.FragmentFavouritesBinding
import com.example.handicraft.ui.product_feature.fragments.ProductDetailsFragment
import com.example.handicraft_graduation_project_2025.data.models.Product
import com.example.handicraft_graduation_project_2025.ui.product_feature.adapters.OnProductClickListener
import com.example.handicraft_graduation_project_2025.ui.product_feature.adapters.ProductGridAdapter
import com.example.handicraft.ui.product_feature.adapters.ProductListAdapter
import com.example.handicraft.ui.product_feature.viewmodels.FavouriteViewModel
import com.example.handicraft_graduation_project_2025.utils.Resource
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil

class FavouritesFragment : Fragment(), OnProductClickListener {
    private lateinit var currentUser: User
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FavouriteViewModel
    private lateinit var gridProductsAdapter: ProductGridAdapter
    private lateinit var linearProductsAdapter: ProductListAdapter
    private var currentProductList: List<Product> = emptyList()
    private var usersMap: Map<String, User> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initViewModels()
        setupGridRecyclerView()
        setupListRecyclerView()
        setupUICallback()
        observeViewModels()

        viewModel.fetchUserById(SharedPrefUtil.getUid(requireContext())!!)
    }

    private fun initViewModels() {
        viewModel = ViewModelProvider(this)[FavouriteViewModel::class.java]
    }

    private fun observeViewModels() {
        viewModel.user.observe(viewLifecycleOwner) {

            it?.let {
                currentUser = it
                viewModel.fetchFavouriteProducts(currentUser.uid)
            }
        }
        viewModel.favouriteProducts.observe(viewLifecycleOwner) { products ->
            when (products) {
                is Resource.Error -> {
                    currentProductList = emptyList()
                    usersMap = emptyMap()
                    updateProductList()
                }

                is Resource.Loading -> {}

                is Resource.Success -> {
                    currentProductList = products.data
                    viewModel.fetchUsersByIds(products.data.map { it.userId })
                }
            }
        }

        viewModel.users.observe(viewLifecycleOwner) { users ->
            usersMap = users.associateBy { it.uid }
            updateProductList()
        }
    }

    private fun setupUICallback() {
        binding.btnGrid.setOnClickListener {
            val selectedColor = ContextCompat.getColor(requireContext(), R.color.background_white)
            val unSelectedColor = ContextCompat.getColor(requireContext(), R.color.purple_500)
            binding.rvGridFavourites.visibility = View.VISIBLE
            binding.rvListFavourites.visibility = View.GONE
            binding.iconGrid.background =
                AppCompatResources.getDrawable(requireContext(), R.drawable.bg_sort_selected)
            binding.iconGrid.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN)

            binding.iconList.background =
                AppCompatResources.getDrawable(requireContext(), R.drawable.bg_sort_unselected)
            binding.iconList.setColorFilter(unSelectedColor, PorterDuff.Mode.SRC_IN)
        }
        binding.btnList.setOnClickListener {
            val selectedColor = ContextCompat.getColor(requireContext(), R.color.background_white)
            val unSelectedColor = ContextCompat.getColor(requireContext(), R.color.purple_500)
            binding.rvGridFavourites.visibility = View.GONE
            binding.rvListFavourites.visibility = View.VISIBLE
            binding.iconGrid.background =
                AppCompatResources.getDrawable(requireContext(), R.drawable.bg_sort_unselected)
            binding.iconGrid.setColorFilter(unSelectedColor, PorterDuff.Mode.SRC_IN)

            binding.iconList.background =
                AppCompatResources.getDrawable(requireContext(), R.drawable.bg_sort_selected)
            binding.iconList.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun updateProductList() {
        gridProductsAdapter.updateList(currentProductList, usersMap, currentUser)
        linearProductsAdapter.updateList(currentProductList, usersMap, currentUser)
        binding.noFavourites.visibility =
            if (currentProductList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupGridRecyclerView() {
        gridProductsAdapter = ProductGridAdapter(emptyList(), this)
        binding.rvGridFavourites.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = gridProductsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListRecyclerView() {
        linearProductsAdapter = ProductListAdapter(emptyList(), this)
        binding.rvListFavourites.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = linearProductsAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        linearProductsAdapter.toggleFavouriteState(productId)
        gridProductsAdapter.toggleFavouriteState(productId)
    }

    private fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
        /*requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            if (addToBackStack) addToBackStack(null)
            commit()
        }*/
    }
}