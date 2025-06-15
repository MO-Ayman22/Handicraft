package com.example.handicraft.data.repository



import com.example.handicraft.data.models.Post
import com.example.handicraft_graduation_project_2025.data.models.Comment
import com.example.handicraft_graduation_project_2025.data.models.Like
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostRepository(private val firestore: FirebaseFirestore) {
    suspend fun getPosts(query: String, lastDocumentId: String? = null, limit: Long = 10): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            var querySnapshot = firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
            if (lastDocumentId != null) {
                val lastDoc = firestore.collection("posts").document(lastDocumentId).get().await()
                querySnapshot = querySnapshot.startAfter(lastDoc)
            }
            if (query.isNotEmpty()) {
                querySnapshot = querySnapshot.whereGreaterThanOrEqualTo("description", query)
                    .whereLessThanOrEqualTo("description", query + "\uf8ff")
            }
            val snapshot = querySnapshot.get().await()
            val posts = snapshot.toObjects(Post::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addLike(postId: String, like: Like) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("posts").document(postId)
                .collection("likes").document(like.userId).set(like).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeLike(postId: String, userId: String) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("posts").document(postId)
                .collection("likes").document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val subscription = firestore.collection("posts").document(postId)
            .collection("comments").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val comments = snapshot?.toObjects(Comment::class.java) ?: emptyList()
                trySend(comments).isSuccess
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addComment(postId: String, comment: Comment) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("posts").document(postId)
                .collection("comments").document().set(comment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLikes(postId: String): Flow<List<Like>> = callbackFlow {
        val subscription = firestore.collection("posts").document(postId)
            .collection("likes").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val likes = snapshot?.toObjects(Like::class.java) ?: emptyList()
                trySend(likes).isSuccess
            }
        awaitClose { subscription.remove() }
    }
}