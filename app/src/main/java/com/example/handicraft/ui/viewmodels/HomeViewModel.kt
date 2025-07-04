package com.example.handicraft.ui.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handicraft.data.models.Post
import com.example.handicraft.data.models.User
import com.example.handicraft.data.repository.PostRepository
import com.example.handicraft.data.repository.UserRepository
import com.example.handicraft_graduation_project_2025.data.models.Comment
import com.example.handicraft_graduation_project_2025.data.models.Like

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val postRepository = PostRepository(FirebaseFirestore.getInstance())
    private val userRepository =  UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val _comments = MutableLiveData<List<Comment>>()
    private val _likes = MutableLiveData<List<Like>>()

    private var currentQuery: String = ""
    private var lastDocumentId: String? = null
    private var isLoading = false

    init {
        loadPosts()
    }

    fun loadPosts() {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            postRepository.getPosts(currentQuery, lastDocumentId).onSuccess { newPosts ->
                val currentPosts = _posts.value.orEmpty().toMutableList()
                currentPosts.addAll(newPosts)
                _posts.value = currentPosts
                lastDocumentId = newPosts.lastOrNull()?.postId
                isLoading = false
            }.onFailure {
                isLoading = false
            }
        }
    }
    // ---------------- Fetch single user by ID ----------------
    fun fetchUserById(userId: String) {
        viewModelScope.launch {
            val result = userRepository.getUser(userId)
            if (result.isSuccess) {
                _user.postValue(result.getOrNull())
            } else {
                _user.postValue(null)
            }
        }
    }

    // ---------------- Fetch list of users by IDs ----------------
    fun fetchUsersByIds(userIds: List<String>) {
        viewModelScope.launch {
            val result = userRepository.getUsersByIds(userIds)
            if (result.isSuccess) {
                _users.postValue(result.getOrNull() ?: emptyList())
            } else {
                _users.postValue(emptyList())
            }
        }
    }
    fun searchPosts(query: String) {
        currentQuery = query
        lastDocumentId = null
        _posts.value = emptyList()
        loadPosts()
    }

    fun toggleLike(postId: String, isLiked: Boolean) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            if (isLiked) {
                postRepository.removeLike(postId, userId)
            } else {
                postRepository.addLike(postId, Like(userId = userId))
            }
            // Refresh posts to update UI
            loadPosts()
        }
    }

    fun getComments(postId: String): LiveData<List<Comment>> {
        viewModelScope.launch {
            postRepository.getComments(postId).collect {
                _comments.value = it
            }
        }
        return _comments
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            postRepository.addComment(postId, Comment(userId = FirebaseAuth.getInstance().currentUser?.uid ?: "", content = content))
        }
    }

    fun getLikes(postId: String): LiveData<List<Like>> {
        viewModelScope.launch {
            postRepository.getLikes(postId).collect {
                _likes.value = it
            }
        }
        return _likes
    }
}