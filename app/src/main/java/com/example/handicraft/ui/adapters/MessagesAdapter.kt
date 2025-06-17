package com.example.handicraft.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.handicraft.databinding.ItemMessageReceivedBinding
import com.example.handicraft.databinding.ItemMessageSentBinding
import com.example.handicraft.ui.fragments.Message
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private val messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class SentMessageViewHolder(val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root)
    class ReceivedMessageViewHolder(val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

        when (holder) {
            is SentMessageViewHolder -> {
                holder.binding.tvMessage.text = message.text
                holder.binding.tvTimestamp.text = timestamp
            }
            is ReceivedMessageViewHolder -> {
                holder.binding.tvMessage.text = message.text
                holder.binding.tvTimestamp.text = timestamp
            }
        }
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT
        else VIEW_TYPE_RECEIVED
    }

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }
}
