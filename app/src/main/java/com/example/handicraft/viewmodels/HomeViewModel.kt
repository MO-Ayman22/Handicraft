package com.example.handicraft_graduation_project_2025.ui.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handicraft.data.models.Post
import com.example.handicraft.data.repository.PostRepository
import com.example.handicraft_graduation_project_2025.data.models.Comment
import com.example.handicraft_graduation_project_2025.data.models.Like

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val postRepository = PostRepository(FirebaseFirestore.getInstance())
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

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

    fun searchPosts(query: String) {
        currentQuery = query
        lastDocumentId = null
        _posts.value = emptyList()
        loadPosts()
    }

    fun toggleLike(post: Post, isLiked: Boolean) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            if (isLiked) {
                postRepository.removeLike(post.postId, userId)
            } else {
                postRepository.addLike(post.postId, Like(userId = userId))
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