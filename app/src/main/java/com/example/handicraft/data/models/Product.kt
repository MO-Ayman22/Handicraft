package com.example.handicraft_graduation_project_2025.data.models
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Product(
    val productId: String = "", // Unique ID for the product
    val userId: String = "", // ID of the user who added the product
    val title: String = "", // Product title from etProductTitle
    val category: String = "", // Product title from etProductTitle
    val price: Double = 0.0, // Product price from etPrice
    val description: String = "", // Description of the product from etDescription
    var imageUrls: List<String> = emptyList(), // List of image URLs uploaded for the product
    val sizes: List<String> = emptyList(), // Selected available sizes (small, medium, large)
    val materialType: String = "", // Material type from actvMaterialType
    val colors: List<Int> = emptyList(), // Selected colors represented by their int value
    @ServerTimestamp val createdAt: Date? = null // Timestamp for when the product was created
)