package com.example.handicraft_graduation_project_2025.data.models

data class Comment(

    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val replies: List<Comment> = emptyList()
)