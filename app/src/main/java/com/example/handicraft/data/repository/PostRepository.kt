package com.example.handicraft.data.repository



import com.example.handicraft.data.models.Post
import com.example.handicraft.utils.Resource
import com.example.handicraft_graduation_project_2025.data.models.Comment
import com.example.handicraft_graduation_project_2025.data.models.Like
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostRepository() {
    private val firestore = FirebaseFirestore.getInstance()
    private val postsCollection = firestore.collection("posts")
    suspend fun getAllPosts(query: String = ""): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            var firestoreQuery: Query = firestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)

            if (query.isNotBlank()) {
                firestoreQuery = firestoreQuery
                    .whereGreaterThanOrEqualTo("description", query)
                    .whereLessThanOrEqualTo("description", query + "\uf8ff")
            }

            val snapshot = firestoreQuery.get().await()
            val posts = snapshot.toObjects(Post::class.java)

            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun toggleLike(postId: String, userId: String): Resource<Unit> {
        return try {
            val postRef = postsCollection.document(postId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val likes = snapshot.get("likes") as? List<String> ?: emptyList()
                val currentLikes = likes.toMutableList()

                val isLiked = currentLikes.contains(userId)
                if (isLiked) {
                    currentLikes.remove(userId)
                    transaction.update(postRef, "likesCount", FieldValue.increment(-1))
                } else {
                    currentLikes.add(userId)
                    transaction.update(postRef, "likesCount", FieldValue.increment(1))
                }

                transaction.update(postRef, "likes", currentLikes)
            }.await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Toggle like failed")
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
    suspend fun getPostsByUser(userId: String): List<Post> {
        return try {
            val snapshot = firestore.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.toObjects(Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun savePost(post: Post,userId: String): Resource<Unit> {
        return try {
            val postRef = FirebaseFirestore.getInstance()
                .collection("posts")
                .document()

            post.postId = postRef.id // assign auto-generated ID
            post.userId = userId
            postRef.set(post).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unexpected error occurred")
        }
    }
}