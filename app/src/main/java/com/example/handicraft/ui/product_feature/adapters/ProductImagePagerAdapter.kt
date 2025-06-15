package com.example.handicraft_graduation_project_2025.ui.product_feature.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.handicraft.databinding.ItemProductImagePagerBinding

class ProductImagePagerAdapter(
    private val images: List<Uri>
) : RecyclerView.Adapter<ProductImagePagerAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemProductImagePagerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (images.isEmpty()) {
            holder.showPlaceholder()
        } else {
            val image = images[position]
            holder.bind(image)
        }
    }

    override fun getItemCount(): Int = if (images.isEmpty()) 1 else images.size

    inner class ImageViewHolder(private val binding: ItemProductImagePagerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUri: Uri) {
            binding.tvPlaceholder.visibility = View.GONE
            binding.ivProductImage.visibility = View.VISIBLE

            Glide.with(binding.root.context)
                .load(imageUri)
                .centerCrop()
                .into(binding.ivProductImage)
        }

        fun showPlaceholder() {
            binding.tvPlaceholder.visibility = View.VISIBLE
            binding.ivProductImage.visibility = View.GONE
        }
    }
}