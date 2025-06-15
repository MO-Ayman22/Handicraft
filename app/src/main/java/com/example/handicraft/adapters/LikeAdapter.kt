package com.example.handicraft_graduation_project_2025.ui.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.handicraft.databinding.ItemLikeBinding
import com.example.handicraft_graduation_project_2025.data.models.Like

class LikeAdapter : ListAdapter<Like, LikeAdapter.LikeViewHolder>(LikeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeViewHolder {
        val binding = ItemLikeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LikeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LikeViewHolder(private val binding: ItemLikeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(like: Like) {
            binding.userName.text = like.userId // Replace with actual username
        }
    }
}

class LikeDiffCallback : DiffUtil.ItemCallback<Like>() {
    override fun areItemsTheSame(oldItem: Like, newItem: Like): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Like, newItem: Like): Boolean = oldItem == newItem
}