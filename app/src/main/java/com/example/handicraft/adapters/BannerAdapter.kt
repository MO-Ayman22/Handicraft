package com.example.handicraft.adapters



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder


import com.bumptech.glide.Glide;
import com.example.handicraft.R
import com.example.handicraft.databinding.ItemBannerBinding


class BannerAdapter : Adapter<BannerAdapter.BannerViewHolder>() {
    private val bannerImages = listOf(
        R.drawable.carousel1,
        R.drawable.carousel2
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false);
        return BannerViewHolder(binding);
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(bannerImages[position % 2])
    }

    override fun getItemCount(): Int = Int.MAX_VALUE;

    inner class BannerViewHolder(private val binding: ItemBannerBinding) : ViewHolder(binding.root) {
        fun bind(imageResId: Int) {
            Glide.with(binding.root)
                .load(imageResId)
                .into(binding.bannerImage)
        }
    }
}