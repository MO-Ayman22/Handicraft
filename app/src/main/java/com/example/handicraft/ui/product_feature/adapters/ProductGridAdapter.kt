package com.example.handicraft.ui.product_feature.adapters

import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.handicraft.R
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.ItemProductGridBinding
import com.example.handicraft.ui.profile_feature.adapters.UiProduct
import com.example.handicraft_graduation_project_2025.data.models.Product



data class UiProduct(
    val product: Product,
    val user: User? = null,
    var isFavourite: Boolean = false
)
class ProductGridAdapter(
    private var uiProducts: List<UiProduct>,
    private val onProductClickListener: OnProductClickListener
) : RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductGridBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.layoutParams =
            (binding.root.layoutParams as ViewGroup.MarginLayoutParams).apply {
                width = ((Resources.getSystem().displayMetrics.widthPixels - 64.dpToPx()) / 2)
            }
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
                butFavorite.setImageResource(R.drawable.ic_favourite)
            else
                butFavorite.setImageResource(R.drawable.ic_like)
            root.setOnClickListener {
                onProductClickListener.onProductClick(product.productId, position)
            }
            butFavorite.setOnClickListener {
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

    private fun Int.dpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()
}

interface OnProductClickListener {
    fun onProductClick(productId: String, position: Int)
    fun onFavouriteToggle(productId: String, newState: Boolean)
}

