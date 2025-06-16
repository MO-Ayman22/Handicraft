package com.example.handicraft.ui.profile_feature.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.handicraft.R
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.FollowerItemBinding
import com.example.handicraft.utils.Constants
import com.example.handicraft.ui.profile_feature.diffutils.FollowerDiffUtil

data class FollowerItem(
    val user: User,
    var isFollowing: Boolean
)

class FollowerRecyclerAdapter(
    private val context: Context,
    private var followers: List<FollowerItem>,
    private val listener: OnFollowerItemClickListener
) : RecyclerView.Adapter<FollowerRecyclerAdapter.FollowerViewHolder>() {

    inner class FollowerViewHolder(val binding: FollowerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FollowerItem) {
            val user = item.user

            Glide.with(binding.followerImage.context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.ic_user)
                .into(binding.followerImage)

            binding.followerName.text = user.username
            binding.followerEmail.text = user.email

            val state = context.getString(R.string.hand_maker)
            binding.followerState.text = if (user.userType == Constants.HANDIMAKER_KEY) state else ""

            if (item.isFollowing) {
                binding.followBut.text = context.getString(R.string.unfollow)
                binding.followBut.background =
                    AppCompatResources.getDrawable(context, R.drawable.unfollow_but_style)
                binding.followBut.setTextColor(context.getColor(R.color.purple_500))

            } else {
                binding.followBut.text = context.getString(R.string.follow)
                binding.followBut.background =
                    AppCompatResources.getDrawable(context, R.drawable.follow_but_style)
                binding.followBut.setTextColor(context.getColor(R.color.background_white))
            }

            binding.followBut.setOnClickListener {
                listener.onFollowButtonClicked(item)
            }
            binding.followerName.setOnClickListener {
                listener.onItemClicked(item)
            }
            binding.followerImage.setOnClickListener {
                listener.onItemClicked(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val binding =
            FollowerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FollowerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        holder.bind(followers[position])
    }

    override fun getItemCount(): Int = followers.size

    fun updateFollowers(newUsers: List<User>, currentUserId: String) {
        val newList = newUsers.map { user ->
            val isFollowing = user.following.contains(currentUserId)
            FollowerItem(user, isFollowing)
        }
        val diffCallback = FollowerDiffUtil(followers, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        followers = newList.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    fun toggleFollowState(followerItem: FollowerItem) {
        val index = followers.indexOfFirst { it.user.uid == followerItem.user.uid }
        if (index != -1) {
            val updatedItem = followerItem.copy(isFollowing = !followerItem.isFollowing)
            followers = followers.toMutableList().apply { set(index, updatedItem) }
            notifyItemChanged(index)
        }
    }

}

interface OnFollowerItemClickListener {
    fun onFollowButtonClicked(followerItem: FollowerItem)
    fun onItemClicked(followerItem: FollowerItem)
}