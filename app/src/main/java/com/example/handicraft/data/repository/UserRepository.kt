package com.example.handicraft.data.repository

import com.example.handicraft.data.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userCollection = firestore.collection("users")

    // ------------------- SAVE / GET USER -------------------

    suspend fun saveUser(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userCollection.document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            if (userId.isEmpty()) return@withContext Result.failure(Exception("Invalid user ID"))
            val document = userCollection.document(userId).get().await()
            val user = document.toObject(User::class.java)?.copy(uid = document.id)
                ?: User(uid = userId, email = auth.currentUser?.email ?: "")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = userCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsersByIds(userIds: List<String>): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val users = mutableListOf<User>()
            userIds.chunked(10).forEach { chunk ->
                val snapshot = userCollection.whereIn("userId", chunk).get().await()
                users += snapshot.toObjects(User::class.java)
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------- BLOCKED ACCOUNTS -------------------

    suspend fun getBlockedAccounts(userId: String): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = userCollection.document(userId)
                .collection("blocked").get().await()
            val blockedAccounts = snapshot.toObjects(User::class.java)
            Result.success(blockedAccounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unblockUser(userId: String, blockedUserId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userCollection.document(userId)
                .collection("blocked").document(blockedUserId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------- FOLLOWERS -------------------

    suspend fun getFollowers(userId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val doc = userCollection.document(userId).get().await()
            val followers = doc.get("followers") as? List<String> ?: emptyList()
            Result.success(followers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addFollower(userId: String, followerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userCollection.document(userId)
                .update("followers", FieldValue.arrayUnion(followerId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFollower(userId: String, followerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userCollection.document(userId)
                .update("followers", FieldValue.arrayRemove(followerId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------- FOLLOWING -------------------

    suspend fun getFollowing(userId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val doc = userCollection.document(userId).get().await()
            val following = doc.get("following") as? List<String> ?: emptyList()
            Result.success(following)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun followUser(currentUserId: String, targetUserId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.runBatch { batch ->
                val currentUserRef = userCollection.document(currentUserId)
                val targetUserRef = userCollection.document(targetUserId)
                batch.update(currentUserRef, "following", FieldValue.arrayUnion(targetUserId))
                batch.update(targetUserRef, "followers", FieldValue.arrayUnion(currentUserId))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(currentUserId: String, targetUserId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.runBatch { batch ->
                val currentUserRef = userCollection.document(currentUserId)
                val targetUserRef = userCollection.document(targetUserId)
                batch.update(currentUserRef, "following", FieldValue.arrayRemove(targetUserId))
                batch.update(targetUserRef, "followers", FieldValue.arrayRemove(currentUserId))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------- FAVORITES -------------------

    suspend fun addProductToFavorites(userId: String, productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userCollection.document(userId)
                .update("favorites", FieldValue.arrayUnion(productId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeProductFromFavorites(userId: String, productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userCollection.document(userId)
                .update("favorites", FieldValue.arrayRemove(productId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
