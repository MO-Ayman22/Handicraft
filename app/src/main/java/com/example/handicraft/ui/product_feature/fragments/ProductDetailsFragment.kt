package com.example.handicraft.ui.product_feature.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.FragmentProductDetailsBinding
import com.example.handicraft_graduation_project_2025.data.models.Product
import com.example.handicraft.ui.product_feature.adapters.ColorAdapter
import com.example.handicraft.ui.product_feature.adapters.OnColorsClickListener
import com.example.handicraft.ui.product_feature.adapters.ProductImageAdapter
import com.example.handicraft.ui.product_feature.viewmodels.ProductsViewModel
import com.example.handicraft.utils.Constants


class ProductDetailsFragment : Fragment(), OnColorsClickListener {
    private lateinit var productId: String
    private var currentProduct: Product? = null
    private var productMaker: User? = null
    private lateinit var viewModel: ProductsViewModel
    private lateinit var colorAdapter: ColorAdapter
    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageAdapter: ProductImageAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        productId = arguments?.getString(Constants.PRODUCT_KEY) ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProductsViewModel::class.java)

        viewModel.user.observe(viewLifecycleOwner) { user ->
            productMaker = user
            setupProductDetails()
            setupImageCarousel()
            setupSizeSelection()
            setupContactButton()
            setupColorRecyclerView()
        }
        viewModel.product.observe(viewLifecycleOwner) { product ->
            currentProduct = product
            currentProduct?.userId?.let { viewModel.fetchUserById(it) }

        }
        viewModel.fetchProductById(productId)


    }

    private fun setupColorRecyclerView() {
        val availableColors = currentProduct?.colors ?: emptyList()
        colorAdapter = ColorAdapter(
            availableColors.toList(),this
        )
        binding.rvColors.adapter = colorAdapter
    }

    private fun setupProductDetails() {
        binding.productTitle.text = currentProduct?.title ?: ""
        binding.descriptionValue.text = currentProduct?.description ?: ""
        binding.priceValue.text = "${currentProduct?.price} EGP"
        binding.categoryValue.text = currentProduct?.category ?: ""
        binding.materialValue.text = currentProduct?.materialType ?: ""
        binding.userName.text = productMaker?.username
        Glide.with(this)
            .load(productMaker?.profileImageUrl)
            .centerCrop()
            .into(binding.userAvatar)
    }

    private fun setupImageCarousel() {
        currentProduct?.imageUrls?.let { imageUrls ->
            imageAdapter = ProductImageAdapter(imageUrls)
            binding.imageCarousel.adapter = imageAdapter
        }
    }


    private fun setupSizeSelection() {
        val availableSizes = currentProduct?.sizes?.toSet() ?: emptySet()
        binding.sizeSmall.visibility = if ("Small" in availableSizes) View.VISIBLE else View.GONE
        binding.sizeMedium.visibility = if ("Medium" in availableSizes) View.VISIBLE else View.GONE
        binding.sizeLarge.visibility = if ("Large" in availableSizes) View.VISIBLE else View.GONE
    }

    private fun setupContactButton() {
        binding.contactButton.setOnClickListener {
            // Handle contact action
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(productId: String) = ProductDetailsFragment().apply {
            arguments = Bundle().apply {
                putString(Constants.PRODUCT_KEY, productId)
            }
        }
    }

    override fun onAddColorClicked() {

    }

    override fun onColorClicked(view: View, position: Int) {
    }
}