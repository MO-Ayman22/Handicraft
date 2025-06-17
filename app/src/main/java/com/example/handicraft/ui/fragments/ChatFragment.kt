package com.example.handicraft.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handicraft.databinding.FragmentChatBinding
import com.example.handicraft.ui.adapters.MessagesAdapter
import com.example.handicraft.ui.profile_feature.fragments.FollowersFragment
import com.example.handicraft.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.getValue

data class Message(val senderId: String, val text: String, val timestamp: Long)

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MessagesAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var chatId: String
    private lateinit var otherUserId: String
    private val database = FirebaseDatabase.getInstance().reference
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
//    private val args: ChatFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        chatId = arguments?.getString(Constants.USER_CURRENT)!!
        otherUserId = arguments?.getString(Constants.USER_TARGET)!!

        binding.rvMessages.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        adapter = MessagesAdapter(messages, currentUserId)
        binding.rvMessages.adapter = adapter

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                binding.etMessage.text?.clear()
            }
        }

        loadMessages()
        return binding.root
    }

    private fun sendMessage(text: String) {
        val messageId = database.child("messages").child(chatId).push().key ?: return
        val message = Message(currentUserId, text, System.currentTimeMillis())
        database.child("messages").child(chatId).child(messageId).setValue(message)
        database.child("chats").child(chatId).updateChildren(
            mapOf(
                "lastMessage" to text,
                "timestamp" to System.currentTimeMillis(),
                "unreadCount" to 0 // Reset unread count for sender
            )
        )
        // Increment unread count for the other user
        database.child("chats").child(chatId).child("unreadCount").get().addOnSuccessListener { snapshot ->
            val currentCount = snapshot.getValue(Int::class.java) ?: 0
            database.child("chats").child(chatId).child("unreadCount").setValue(currentCount + 1)
        }
    }

    private fun loadMessages() {
        database.child("messages").child(chatId)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(Message::class.java)
                        message?.let { messages.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        fun newInstance(currentUserId: String,targetUserId: String): FollowersFragment {
            return FollowersFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.USER_CURRENT, currentUserId)
                    putString(Constants.USER_TARGET, targetUserId)
                }
            }
        }
    }
}