package com.example.handicraft.ui.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.handicraft.R
import com.example.handicraft.data.models.Post
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.ItemImagePostBinding
import com.example.handicraft.databinding.ItemPostBinding

class PostAdapter(
    private val posts: List<Post>,
    private val usersMap: Map<String, User>,
    private val listener: OnPostClickListener
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position], usersMap[posts[position].userId]!!)
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post, user: User) {
            binding.apply {
                tvUsername.text = user.username
                tvContent.text = post.content
                Glide.with(binding.root)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.ic_user)
                    .into(imgUser)
                likesCount.text = post.likesCount.toString()
                commentsCount.text = post.commentsCount.toString()
                if (post.isLiked)
                    binding.likeIcon.setImageResource(R.drawable.ic_like_active)
                else
                    binding.likeIcon.setImageResource(R.drawable.ic_like)

            }
            displayImages(post.imageUrls)
            /*binding.iconOptions.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view, Gravity.NO_GRAVITY, 0, R.style.PopupMenuStyle)
                popup.menuInflater.inflate(R.menu.edit_delete_menu, popup.menu)

                // Force show icons in the popup menu
                try {
                    val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                    fieldMPopup.isAccessible = true
                    val mPopup = fieldMPopup.get(popup)
                    mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                        .invoke(mPopup, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Set text color for each menu item
                for (i in 0 until popup.menu.size()) {
                    val menuItem = popup.menu.getItem(i)
                    val span = SpannableString(menuItem.title)
                    val editColor = ContextCompat.getColor(view.context, R.color.text_primary)
                    val deleteColor = ContextCompat.getColor(view.context, R.color.text_delete)
                    when (menuItem.itemId) {
                        R.id.menu_edit -> span.setSpan(ForegroundColorSpan(editColor), 0, span.length, 0)
                        R.id.menu_delete -> span.setSpan(ForegroundColorSpan(deleteColor), 0, span.length, 0)
                    }
                    menuItem.title = span
                }

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit -> {
                            listener.onEditPost(postId)
                            true
                        }
                        R.id.menu_delete -> {
                            listener.onDeletePost(postId)
                            true
                        }
                        else -> false
                    }
                }

                popup.show()
            }*/
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

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
interface OnPostClickListener{
    fun onCommentClick (position: Int,postId:String)
    fun onLikeClick(position: Int,postId: String,isLiked:Boolean)
    fun onLikesCountClick(position: Int,postId: String)
}