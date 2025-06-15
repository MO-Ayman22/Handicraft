package com.example.handicraft.viewmodels

import com.example.handicraft_graduation_project_2025.data.models.Notification
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handicraft.data.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val notificationRepository = NotificationRepository(FirebaseFirestore.getInstance())
    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> get() = _notifications

    init {
        viewModelScope.launch {
            notificationRepository.getNotifications("current_user_id").onSuccess {
                _notifications.value = it
            }
        }
    }
}