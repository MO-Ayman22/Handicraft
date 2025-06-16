package com.example.handicraft.ui.product_feature.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.handicraft.R
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.ItemProductLinearBinding
import com.example.handicraft_graduation_project_2025.data.models.Product
import com.example.handicraft.ui.profile_feature.adapters.UiProduct


class ProductListAdapter(
    private var uiProducts: List<UiProduct>,
    private val onProductClickListener: OnProductClickListener
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductLinearBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding =
            ItemProductLinearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val (product, user, isFavourite) = uiProducts[position]
        Log.d("TAG", "here ${uiProducts[position]}")
        holder.binding.apply {
            productName.text = product.title
            productDetails.text = "Made of ${product.materialType}"
            productMaker.text = user?.username ?: "Unknown"

            if (product.imageUrls.isNotEmpty()) {
                Glide.with(productImage.context)
                    .load(product.imageUrls.first())
                    .into(productImage)
            }

            if (!user?.profileImageUrl.isNullOrEmpty()) {
                Glide.with(imageOfMaker.context)
                    .load(user?.profileImageUrl)
                    .into(imageOfMaker)
            }
            if (isFavourite)
                favoriteBut.setImageResource(R.drawable.ic_favourite)
            else
                favoriteBut.setImageResource(R.drawable.ic_like)
            root.setOnClickListener {
                onProductClickListener.onProductClick(product.productId, position)
            }
            favoriteBut.setOnClickListener {
                onProductClickListener.onFavouriteToggle(product.productId, !isFavourite)
            }
        }
    }

    override fun getItemCount(): Int = uiProducts.size

    fun updateList(productsList: List<Product>, usersMap: Map<String, User>, currentUser: User) {
        val newList = productsList.map { product ->
            UiProduct(
                product = product,
                user = usersMap[product.userId],
                isFavourite = currentUser.favorites.contains(product.productId)
            )
        }
        uiProducts = newList
        notifyDataSetChanged()
        // val diffResult = DiffUtil.calculateDiff(UiProductDiffCallback(uiProducts, newList))
        //diffResult.dispatchUpdatesTo(this)
    }

    fun toggleFavouriteState(productId: String) {
        val index = uiProducts.indexOfFirst { it.product.productId == productId }
        if (index != -1) {
            val oldItem = uiProducts[index]
            val updatedItem = oldItem.copy(isFavourite = !oldItem.isFavourite)
            val newList = uiProducts.toMutableList()
            newList[index] = updatedItem
            uiProducts = newList
            notifyItemChanged(index)
        }
    }
}


