package com.example.handicraft.ui.product_feature.viewmodels

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

class FavouriteViewModel : ViewModel() {

    private val productRepository = ProductRepository()
    private val userRepository = UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())

    private val _favouriteProducts = MutableLiveData<Resource<List<Product>>>()
    val favouriteProducts: LiveData<Resource<List<Product>>> get() = _favouriteProducts

    private val _favoriteActionStatus = MutableLiveData<Resource<Unit>>()
    val favoriteActionStatus: LiveData<Resource<Unit>> get() = _favoriteActionStatus

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    // ---------------- Get favorite products ----------------
    fun fetchFavouriteProducts(userId: String) {
        viewModelScope.launch {
            _favouriteProducts.postValue(Resource.Loading)
            val result = productRepository.getFavouriteProducts(userId)
            if (result.isSuccess) {
                _favouriteProducts.postValue(Resource.Success(result.getOrNull() ?: emptyList()))
            } else {
                _favouriteProducts.postValue(Resource.Error(result.exceptionOrNull()?.message ?: "Failed to load favorites"))
            }
        }
    }

    // ---------------- Add product to favorites ----------------
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
}
