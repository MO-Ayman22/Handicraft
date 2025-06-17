package com.example.handicraft.ui.product_feature.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handicraft.data.models.User
import com.example.handicraft.data.repository.ProductRepository
import com.example.handicraft.data.repository.UserRepository
import com.example.handicraft_graduation_project_2025.data.models.Product
import com.example.handicraft.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {

    private val productRepository = ProductRepository()
    private val userRepository = UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())

    private val _favoriteProducts = MutableLiveData<List<Product>>()
    val favoriteProducts: LiveData<List<Product>> get() = _favoriteProducts

    private val _favoriteActionStatus = MutableLiveData<Resource<Unit>>()
    val favoriteActionStatus: LiveData<Resource<Unit>> get() = _favoriteActionStatus

    private val _addProductStatus = MutableLiveData<Result<Unit>>()
    val addProductStatus: LiveData<Result<Unit>> get() = _addProductStatus

    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> get() = _product

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val _categoryProducts = MutableLiveData<Resource<List<Product>>>()
    val categoryProducts: LiveData<Resource<List<Product>>> get() = _categoryProducts

    // ---------------- Fetch products by category ----------------
    fun fetchProductsByCategory(category: String) {
        viewModelScope.launch {
            _categoryProducts.postValue(Resource.Loading)
            val result = productRepository.getProductsByCategory(category)
            if (result.isSuccess) {
                _categoryProducts.postValue(Resource.Success(result.getOrNull() ?: emptyList()))
            } else {
                _categoryProducts.postValue(Resource.Error(result.exceptionOrNull()?.message ?: "Unknown error"))
            }
        }
    }

    // ---------------- Fetch user by ID ----------------
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

    // ---------------- Fetch users by multiple IDs ----------------
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

    // ---------------- Add product to favorites ----------------
    fun addToFavorites(userId: String, productId: String) {
        viewModelScope.launch {
            _favoriteActionStatus.postValue(Resource.Loading)
            val result = userRepository.addProductToFavorites(userId, productId)
            if (result.isSuccess) {
                _favoriteActionStatus.postValue(Resource.Success(Unit))
            } else {
                _favoriteActionStatus.postValue(Resource.Error(result.exceptionOrNull()?.message ?: "Error adding to favorites"))
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
                _favoriteActionStatus.postValue(Resource.Error(result.exceptionOrNull()?.message ?: "Error removing from favorites"))
            }
        }
    }
}
