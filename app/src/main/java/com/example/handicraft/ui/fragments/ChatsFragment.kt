package com.example.handicraft.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handicraft.data.models.Chat
import com.example.handicraft.databinding.FragmentChatsBinding
import com.example.handicraft.ui.adapters.ChatsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatsAdapter
    private val chats = mutableListOf<Chat>()
    private val database = FirebaseDatabase.getInstance().reference
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)

        binding.rvChats.layoutManager = LinearLayoutManager(context)
        adapter = ChatsAdapter(chats) { chat ->
//            val action = ChatsFragmentDirections.actionChatsFragmentToChatFragment(chat.id, chat.otherUserId)
//            findNavController().navigate(action)
            // Reset unread count when chat is opened
            database.child("chats").child(chat.id).child("unreadCount").setValue(0)
        }
        binding.rvChats.adapter = adapter

        loadChats()
        return binding.root
    }

    private fun loadChats() {
        database.child("chats").orderByChild("users/$currentUserId").equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    chats.clear()
                    for (chatSnapshot in snapshot.children) {
                        val lastMessage = chatSnapshot.child("lastMessage").getValue(String::class.java) ?: ""
                        val timestamp = chatSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                        val unreadCount = chatSnapshot.child("unreadCount").getValue(Int::class.java) ?: 0
                        val profileImageUrl = chatSnapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
                        val users = chatSnapshot.child("users").children.mapNotNull { it.key }
                            .filter { it != currentUserId }
                        if (users.isNotEmpty()) {
                            chats.add(Chat(
                                id = chatSnapshot.key!!,
                                currentUserId = currentUserId,
                                targetUserId = users[0],
                                lastMessage = lastMessage,
                                timestamp = timestamp,
                                unreadCount = unreadCount,
                                profileImageUrl = profileImageUrl
                            ))
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
