package com.example.handicraft.data.repository

import com.example.handicraft.data.models.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NotificationRepository(private val firestore: FirebaseFirestore) {
    suspend fun getNotifications(userId: String): Result<List<Notification>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("notifications").get().await()
            val notifications = snapshot.toObjects(Notification::class.java)
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}