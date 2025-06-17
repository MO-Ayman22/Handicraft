package com.example.handicraft.data.models

import com.example.handicraft_graduation_project_2025.data.models.Comment
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Post(
    var postId: String = "",
    var userId: String = "",
    var content: String = "",
    var imageUrls: List<String> = emptyList(),
    var likes: List<String> = emptyList(),
    var comments: List<Comment> = emptyList(),
    var likesCount: Int = 0,
    var commentsCount: Int = 0,
    @ServerTimestamp val createdAt: Date? = null
)