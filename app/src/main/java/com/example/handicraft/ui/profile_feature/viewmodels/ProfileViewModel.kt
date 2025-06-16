package com.example.handicraft.ui.profile_feature.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handicraft.data.models.User
import com.example.handicraft.data.repository.ProductRepository
import com.example.handicraft.data.repository.UserRepository
import com.example.handicraft_graduation_project_2025.data.models.Product
import com.example.handicraft_graduation_project_2025.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val productRepository = ProductRepository()
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


    fun addToFavorites(userId: String, productId: String) {
        viewModelScope.launch {
            _favoriteActionStatus.postValue(Resource.Loading)
            val result = userRepository.addProductToFavorites(userId, productId)
            if (result.isSuccess) {
                _favoriteActionStatus.postValue(Resource.Success(Unit))
            } else {
                _favoriteActionStatus.postValue(Resource.Error(result.exceptionOrNull()?.message ?: "Failed to add to favorites"))
            }
        }
    }

    // ---------------- Remove product from favorites ----------------
    fun removeFromFavorites(userId: String, productId: String) {
        viewModelScope.launch {
            _favoriteActionStatus.postValue(Resource.Loading)
            val result = userRepository.removeProductFromFavorites(userId, productId)
            if (result.isSuccess) {
                _favoriteActionStatus.postValue(Resource.Success(Unit))
            } else {
                _favoriteActionStatus.postValue(Resource.Error(result.exceptionOrNull()?.message ?: "Failed to remove from favorites"))
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

    fun fetchProductsByUser(userId: String) {
        viewModelScope.launch {
            val result = productRepository.getProductsByUser(userId)
            if (result.isSuccess)
                _userProducts.postValue(result.getOrNull() ?: emptyList())
            else
                _userProducts.postValue(emptyList())
        }
    }


}
