package com.example.handicraft.ui.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.handicraft.R
import com.example.handicraft.data.models.Post
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.ItemImagePostBinding
import com.example.handicraft.databinding.ItemPostBinding

data class UiPost(
    val post: Post,
    val user: User,
    var isLiked: Boolean
)

class PostAdapter(
    private var postsList: List<UiPost>,
    private val listener: OnPostClickListener
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun getItemCount() = postsList.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val (post, user, isLiked) = postsList[position]

        holder.bind(post, user, isLiked)
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post, user: User, isLiked: Boolean) {
            binding.apply {
                tvUsername.text = user.username
                tvContent.text = post.content
                Glide.with(binding.root)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.ic_user)
                    .into(imgUser)
                likesCount.text = post.likesCount.toString()
                commentsCount.text = post.commentsCount.toString()
                if (isLiked)
                    binding.likeIcon.setImageResource(R.drawable.ic_like_active)
                else
                    binding.likeIcon.setImageResource(R.drawable.ic_like)
            }
            displayImages(post.imageUrls)
            binding.likeIcon.setOnClickListener {
                listener.onLikeClick(adapterPosition, post.postId, !isLiked)
                postsList[adapterPosition].isLiked = !isLiked
                notifyItemChanged(adapterPosition)

            }
            binding.likesCount.setOnClickListener {
                listener.onLikesCountClick(adapterPosition, post.postId)
            }
            binding.commentsCount.setOnClickListener {
                listener.onCommentClick(adapterPosition, post.postId)
            }
        }

        private fun displayImages(imageUris: List<String>) {
            val container = binding.glImageContainer
            container.visibility = if (imageUris.isEmpty()) View.GONE else View.VISIBLE
            container.removeAllViews()

            val maxImages = minOf(imageUris.size, 4)
            for (i in 0 until maxImages) {
                val imageBinding = ItemImagePostBinding.inflate(
                    LayoutInflater.from(container.context),
                    container,
                    false
                )

                Glide.with(container.context)
                    .load(imageUris[i])
                    .transform(RoundedCorners(16))
                    .into(imageBinding.imageView)

                if (i == 3 && imageUris.size > 4) {
                    imageBinding.overlay.visibility = View.VISIBLE
                    imageBinding.moreCount.text = "+${imageUris.size - 3}"
                }

                imageBinding.root.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec =
                        GridLayout.spec(GridLayout.UNDEFINED, getColSpan(imageUris.size, i), 1f)
                    rowSpec =
                        GridLayout.spec(GridLayout.UNDEFINED, getRowSpan(imageUris.size, i), 1f)
                }

                container.addView(imageBinding.root)
            }
        }

        private fun getColSpan(count: Int, index: Int) = when (count) {
            1 -> 2
            2 -> 1
            3 -> if (index == 0) 2 else 1
            else -> 1
        }

        private fun getRowSpan(count: Int, index: Int) = when (count) {
            1, 2 -> 2
            else -> 1
        }
    }

    fun updatePosts(posts: List<Post>, usersMap: Map<String, User>, currentUserId: String) {
        val newList = posts.map { post ->
            usersMap[post.userId]?.let {
                UiPost(
                    post = post,
                    user = it,
                    isLiked = post.likes.contains(currentUserId)
                )
            }
        }
        newList.let {
            postsList = it.filterNotNull()
            notifyDataSetChanged()
        }
    }
}
    interface OnPostClickListener {
        fun onCommentClick(position: Int, postId: String)
        fun onLikeClick(position: Int, postId: String, isLiked: Boolean)
        fun onLikesCountClick(position: Int, postId: String)
    }