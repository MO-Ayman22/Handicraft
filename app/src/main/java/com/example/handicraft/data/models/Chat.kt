package com.example.handicraft_graduation_project_2025.data.models

data class Chat(
    val id: String = "",
    val userId: String = "",
    val otherUserId: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val unreadCount: Int = 0
)