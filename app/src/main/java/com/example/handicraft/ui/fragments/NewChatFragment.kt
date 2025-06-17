//package com.example.handicraft.ui.fragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import com.google.android.material.button.MaterialButton
//import com.google.android.material.textfield.TextInputEditText
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.FirebaseDatabase
//import java.util.*
//
//class NewChatFragment : Fragment() {
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_new_chat, container, false)
//        val etUserId = view.findViewById<TextInputEditText>(R.id.et_user_id)
//        val btnStartChat = view.findViewById<MaterialButton>(R.id.btn_start_chat)
//
//        btnStartChat.setOnClickListener {
//            val otherUserId = etUserId.text.toString().trim()
//            if (otherUserId.isNotEmpty()) {
//                startChat(otherUserId)
//            }
//        }
//        return view
//    }
//
//    private fun startChat(otherUserId: String) {
//        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        val chatId = database.child("chats").push().key ?: return
//        val chatData = mapOf(
//            "users" to mapOf(currentUserId to true, otherUserId to true),
//            "lastMessage" to "",
//            "timestamp" to System.currentTimeMillis()
//        )
//        database.child("chats").child(chatId).setValue(chatData)
//        val bundle = Bundle().apply {
//            putString("chatId", chatId)
//            putString("otherUserId", otherUserId)
//        }
//        val chatFragment = ChatFragment().apply { arguments = bundle }
//        (activity as MainActivity).replaceFragment(chatFragment)
//    }
//
//    companion object {
//        private val database = FirebaseDatabase.getInstance().reference
//    }
//}