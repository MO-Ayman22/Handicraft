package com.example.handicraft.ui.profile_feature.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handicraft.data.models.User
import com.example.handicraft.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FollowersViewModel : ViewModel() {

    private val userRepository = UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users


    private val _operationStatus = MutableLiveData<Result<Unit>>()
    val operationStatus: LiveData<Result<Unit>> get() = _operationStatus

    fun fetchUserById(userId: String) {
        viewModelScope.launch {
            val result = userRepository.getUser(userId)
            if (result.isSuccess) {
                _user.postValue(result.getOrNull())
            } else {
                _user.postValue(null)
            }
        }
    }


    fun fetchUsersByIds(userIds: List<String>) {
        viewModelScope.launch {
            val result = userRepository.getUsersByIds(userIds)
            if (result.isSuccess) {
                _users.postValue(result.getOrNull() ?: emptyList())
            } else {
                _users.postValue(emptyList())
            }
        }
    }

    fun followUser(currentUserId: String, targetUserId: String) {
        viewModelScope.launch {
            val result = userRepository.followUser(currentUserId, targetUserId)
            _operationStatus.postValue(result)
        }
    }

    fun unfollowUser(currentUserId: String, targetUserId: String) {
        viewModelScope.launch {
            val result = userRepository.unfollowUser(currentUserId, targetUserId)
            _operationStatus.postValue(result)
        }
    }

    fun fetchFollowers(userId: String) {
        viewModelScope.launch {
            val result = userRepository.getFollowers(userId)
            if (result.isSuccess) {
                fetchUsersByIds(result.getOrDefault(emptyList()))
            } else {
                _users.postValue(emptyList())
            }
        }
    }


}
