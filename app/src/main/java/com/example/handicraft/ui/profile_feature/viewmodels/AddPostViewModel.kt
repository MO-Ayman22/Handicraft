package com.example.handicraft.ui.profile_feature.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handicraft.data.models.Post
import com.example.handicraft.data.models.User
import com.example.handicraft.data.repository.PostRepository
import com.example.handicraft.data.repository.ProductRepository
import com.example.handicraft.data.repository.UserRepository
import com.example.handicraft_graduation_project_2025.data.models.Product
import com.example.handicraft.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AddPostViewModel : ViewModel() {

    private val productRepository = ProductRepository()
    private val postRepository = PostRepository(FirebaseFirestore.getInstance())

    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> = _userPosts

    private val userRepository =
        UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())

    private val _userProducts = MutableLiveData<List<Product>>()
    val userProducts: LiveData<List<Product>> get() = _userProducts
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users
    private val _favoriteProducts = MutableLiveData<List<Product>>()
    val favoriteProducts: LiveData<List<Product>> get() = _favoriteProducts

    private val _favoriteActionStatus = MutableLiveData<Resource<Unit>>()
    val favoriteActionStatus: LiveData<Resource<Unit>> get() = _favoriteActionStatus

    private val _savePostStatus = MutableLiveData<Resource<Unit>>()
    val savePostStatus: LiveData<Resource<Unit>> = _savePostStatus

    fun savePost(post: Post,userId: String) {
        viewModelScope.launch {
            _savePostStatus.postValue(Resource.Loading)
            val result = postRepository.savePost(post,userId)
            _savePostStatus.postValue(result)
        }
    }


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




}
