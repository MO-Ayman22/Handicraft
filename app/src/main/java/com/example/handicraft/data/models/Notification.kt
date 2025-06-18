package com.example.handicraft.data.models

data class Notification(
    var message: String = "",
    var userImageUrl: String="",
    var username: String="",
    var id: String = "",
    var userId: String = "",
    var action: String = "",
    var postId: String? = null,
    var timestamp: Long = 0L
)