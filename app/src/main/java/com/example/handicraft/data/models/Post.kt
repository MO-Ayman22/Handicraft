package com.example.handicraft.data.models

import com.example.handicraft_graduation_project_2025.data.models.Comment
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Post(

    var postId: String = "",
    var userId: String = "",
    var username: String = "",
    var content: String = "",
    var imageUrl: String? = null,
    var likes: List<String> = emptyList(),
    var comments: List<Comment> = emptyList(),
    var userType: String = "",
    var craftType: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    var likesCount: Int = 0,
    var commentsCount: Int = 0,
    var isLiked: Boolean=false
)