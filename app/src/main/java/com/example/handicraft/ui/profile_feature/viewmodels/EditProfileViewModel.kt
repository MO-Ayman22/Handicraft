package com.example.handicraft_graduation_project_2025.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handicraft.data.models.User
import com.example.handicraft.data.repository.AuthRepository
import com.example.handicraft.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {

    private val userRepository =
        UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
    private val authRepository =
        AuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users


    private val _operationStatus = MutableLiveData<Result<Unit>>()
    val operationStatus: LiveData<Result<Unit>> get() = _operationStatus


    private val _updateState = MutableLiveData<Result<Unit>>()
    val updateState: LiveData<Result<Unit>> = _updateState

    /*fun updateProfile(user: User, newEmail: String) {
        viewModelScope.launch {
            val emailResult =
                if (newEmail.isNotBlank()) authRepository.updateEmail(newEmail) else Result.success(
                    Unit
                )
            if (emailResult.isSuccess) {
                _updateState.value = authRepository.updateUserProfile(user)
            } else {
                _updateState.value = emailResult
            }
        }
    }*/

    fun fetchUserById(userId: String) {
        viewModelScope.launch {
            val result = userRepository.getUser(userId)
            if (result.isSuccess)
                _user.postValue(result.getOrNull())
            else
                _user.postValue(null)
        }
    }


}
