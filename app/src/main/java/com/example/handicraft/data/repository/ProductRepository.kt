package com.example.handicraft.data.repository

import com.example.handicraft_graduation_project_2025.data.models.Product
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductRepository {

    private val productCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("products")

    private val userCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("users")

    suspend fun addOrUpdateProduct(product: Product): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val docRef = productCollection.document(product.productId.ifEmpty { productCollection.document().id })
            val productWithId = product.copy(productId = docRef.id)
            docRef.set(productWithId, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            productCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = productCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsByUser(userId: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = productCollection
                .whereEqualTo("userId", userId)
                .get().await()
            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductById(productId: String): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val document = productCollection.document(productId).get().await()
            val product = document.toObject(Product::class.java)
            if (product != null) {
                Result.success(product)
            } else {
                Result.failure(Exception("Product not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsByCategory(category: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = productCollection
                .whereEqualTo("category", category)
                .get().await()
            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavouriteProducts(userId: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val userSnapshot = userCollection.document(userId).get().await()
            val favorites = userSnapshot.get("favorites") as? List<String> ?: emptyList()

            if (favorites.isEmpty()) return@withContext Result.success(emptyList())

            val favouriteProducts = mutableListOf<Product>()
            val chunks = favorites.chunked(10)

            for (chunk in chunks) {
                val querySnapshot = productCollection
                    .whereIn("productId", chunk)
                    .get().await()

                favouriteProducts += querySnapshot.toObjects(Product::class.java)
            }

            Result.success(favouriteProducts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
