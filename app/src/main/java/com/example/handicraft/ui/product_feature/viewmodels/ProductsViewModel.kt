package com.example.handicraft.ui.product_feature.viewmodels

import androidx.lifecycle.*
import com.example.handicraft.data.models.User
import com.example.handicraft.data.repository.ProductRepository
import com.example.handicraft.data.repository.UserRepository
import com.example.handicraft_graduation_project_2025.data.models.Product

import com.example.handicraft.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProductsViewModel : ViewModel() {

    private val productRepository = ProductRepository()
    private val userRepository = UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())

    private val _addProductStatus = MutableLiveData<Resource<Unit>>()
    val addProductStatus: LiveData<Resource<Unit>> get() = _addProductStatus

    private val _deleteProductStatus = MutableLiveData<Resource<Unit>>()
    val deleteProductStatus: LiveData<Resource<Unit>> get() = _deleteProductStatus

    private val _favoriteActionStatus = MutableLiveData<Resource<Unit>>()
    val favoriteActionStatus: LiveData<Resource<Unit>> get() = _favoriteActionStatus

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> get() = _product

    private val _products = MutableLiveData<Resource<List<Product>>>()
    val products: LiveData<Resource<List<Product>>> get() = _products

    private val _userProducts = MutableLiveData<List<Product>>()
    val userProducts: LiveData<List<Product>> get() = _userProducts

    private val _categoryProducts = MutableLiveData<Resource<List<Product>>>()
    val categoryProducts: LiveData<Resource<List<Product>>> get() = _categoryProducts

    private val _searchResults = MutableLiveData<Resource<List<Product>>>()
    val searchResults: LiveData<Resource<List<Product>>> get() = _searchResults

    // ---------------- Add or Update Product ----------------
    fun addOrUpdateProduct(product: Product) {
        viewModelScope.launch {
            _addProductStatus.postValue(Resource.Loading)
            val result = productRepository.addOrUpdateProduct(product)
            if (result.isSuccess) {
                _addProductStatus.postValue(Resource.Success(Unit))
            } else {
                _addProductStatus.postValue(Resource.Error(result.exceptionOrNull()?.message ?: "Failed to save product"))
            }
        }
    }

    // ---------------- Delete Product ----------------
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _deleteProductStatus.postValue(Resource.Loading)
            val result = productRepository.deleteProduct(productId)
            if (result.isSuccess) {
                _deleteProductStatus.postValue(Resource.Success(Unit))
            } else {
                _deleteProductStatus.postValue(Resource.Error(result.exceptionOrNull()?.message ?: "Failed to delete product"))
            }
        }
    }

    // ---------------- Fetch All Products ----------------
    fun fetchAllProducts() {
        viewModelScope.launch {
            _products.postValue(Resource.Loading)
            val result = productRepository.getAllProducts()
            if (result.isSuccess) {
                _products.postValue(Resource.Success(result.getOrNull() ?: emptyList()))
            } else {
                _products.postValue(Resource.Error(result.exceptionOrNull()?.message ?: "Failed to load products"))
            }
        }
    }

    // ---------------- Fetch Product By ID ----------------
    fun fetchProductById(productId: String) {
        viewModelScope.launch {
            val result = productRepository.getProductById(productId)
            if (result.isSuccess) {
                _product.postValue(result.getOrNull())
            } else {
                _product.postValue(null)
            }
        }
    }

    // ---------------- Search Filtered Products ----------------
    fun fetchProductsWithFilter(
        craftsmanName: String?,
        selectedCategories: List<String>,
        selectedSizes: List<String>,
        selectedMaterials: List<String>,
        productTitleQuery: String?
    ) {
        viewModelScope.launch {
            _searchResults.postValue(Resource.Loading)
            val result = productRepository.getAllProducts()
            val usersResult = userRepository.getAllUsers()

            if (result.isSuccess && usersResult.isSuccess) {
                val products = result.getOrNull() ?: emptyList()
                val users = usersResult.getOrNull() ?: emptyList()
                val userMap = users.associateBy { it.uid }

                val filtered = products.filter { product ->
                    val userName = userMap[product.userId]?.username ?: ""

                    val matchCraftsman = craftsmanName.isNullOrBlank() || userName.contains(craftsmanName, ignoreCase = true)
                    val matchCategory = selectedCategories.isEmpty() || selectedCategories.contains(product.category)
                    val matchSize = selectedSizes.isEmpty() || product.sizes.any { it in selectedSizes }
                    val matchMaterial = selectedMaterials.isEmpty() || selectedMaterials.contains(product.materialType)
                    val matchTitle = productTitleQuery.isNullOrBlank() || product.title.contains(productTitleQuery, ignoreCase = true)

                    matchCraftsman && matchCategory && matchSize && matchMaterial && matchTitle
                }

                _searchResults.postValue(Resource.Success(filtered))
            } else {
                _searchResults.postValue(Resource.Error("Failed to apply filters"))
            }
        }
    }

    // ---------------- User-related ----------------
    fun fetchUserById(userId: String) {
        viewModelScope.launch {
            val result = userRepository.getUser(userId)
            _user.postValue(result.getOrNull())
        }
    }

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

    // ---------------- Products by User ----------------
    fun fetchProductsByUser(userId: String) {
        viewModelScope.launch {
            val result = productRepository.getProductsByUser(userId)
            if (result.isSuccess) {
                _userProducts.postValue(result.getOrNull() ?: emptyList())
            } else {
                _userProducts.postValue(emptyList())
            }
        }
    }

    // ---------------- Products by Category ----------------
    fun fetchProductsByCategory(category: String) {
        viewModelScope.launch {
            _categoryProducts.postValue(Resource.Loading)
            val result = productRepository.getProductsByCategory(category)
            if (result.isSuccess) {
                _categoryProducts.postValue(Resource.Success(result.getOrNull() ?: emptyList()))
            } else {
                _categoryProducts.postValue(Resource.Error(result.exceptionOrNull()?.message ?: "Failed to load category products"))
            }
        }
    }

    // ---------------- Favorites ----------------
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
}
