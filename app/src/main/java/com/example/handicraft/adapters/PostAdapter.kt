package com.example.handicraft_graduation_project_2025.ui.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.handicraft.R
import com.example.handicraft.data.models.Post
import com.example.handicraft.databinding.ItemPostBinding

class PostAdapter(
    private val onCommentClick: (Post) -> Unit,
    private val onLikeClick: (Post, Boolean) -> Unit,
    private val onLikesCountClick: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.username.text = post.username
            binding.content.text = post.content
            Glide.with(binding.postImage)
                .load(post.imageUrl)
                .placeholder(R.drawable.ic_user)
                .into(binding.postImage)
            binding.likeIcon.setImageResource(
                if (post.isLiked) R.drawable.ic_like_active else R.drawable.ic_like
            )
            binding.likeCount.text = post.likesCount.toString()
            binding.commentIcon.setImageResource(R.drawable.ic_comment)

            binding.commentIcon.setOnClickListener {
                onCommentClick(post)
            }
            binding.likeIcon.setOnClickListener {
                onLikeClick(post, post.isLiked)
            }
            binding.likeCount.setOnClickListener {
                onLikesCountClick(post)
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}