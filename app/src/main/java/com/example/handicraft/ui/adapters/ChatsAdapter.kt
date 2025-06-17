package com.example.handicraft.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.handicraft.R
import com.example.handicraft.data.models.Chat
import com.example.handicraft.databinding.ItemChatBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatsAdapter(
    private val chats: List<Chat>,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        with(holder.binding) {
            //tvUserId.text = chat.otherUserId
            lastMessage.text = chat.lastMessage
            tvTimestamp.text = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(chat.timestamp))
            if (chat.unreadCount > 0) {
                unreadCount.text = chat.unreadCount.toString()
                unreadCount.visibility = View.VISIBLE
            } else {
                unreadCount.visibility = View.GONE
            }
            Glide.with(root.context)
                .load(chat.profileImageUrl)
                .placeholder(R.drawable.ic_user)
                .circleCrop()
                .into(imgUser)
            root.setOnClickListener { onChatClick(chat) }
        }
    }

    override fun getItemCount() = chats.size
}

