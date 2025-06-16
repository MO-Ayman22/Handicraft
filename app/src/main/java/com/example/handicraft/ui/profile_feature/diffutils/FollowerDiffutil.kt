package com.example.handicraft.ui.profile_feature.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.example.handicraft.ui.profile_feature.adapters.FollowerItem

class FollowerDiffUtil(
    private val oldList: List<FollowerItem>,
    private val newList: List<FollowerItem>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].user.uid == newList[newItemPosition].user.uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}