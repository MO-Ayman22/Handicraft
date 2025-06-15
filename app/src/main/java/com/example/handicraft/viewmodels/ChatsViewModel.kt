//package com.example.handicraft_graduation_project_2025.ui.viewmodels
//
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.handicraft_graduation_project_2025.data.models.Chat
//import com.example.handicraft_graduation_project_2025.data.repository.ChatRepository
//import com.google.firebase.database.FirebaseDatabase
//import kotlinx.coroutines.launch
//
//class ChatsViewModel : ViewModel() {
//    private val chatRepository = ChatRepository(FirebaseDatabase.getInstance())
//    private val _chats = MutableLiveData<List<Chat>>()
//    val chats: LiveData<List<Chat>> get() = _chats
//
//    init {
//        viewModelScope.launch {
//            chatRepository.getChats("current_user_id").onSuccess {
//                _chats.value = it
//            }
//        }
//    }
//}