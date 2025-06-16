package com.example.handicraft.viewmodels


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handicraft.data.models.User
import com.example.handicraft.data.repository.AuthRepository
import com.example.handicraft.data.repository.UserRepository
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
    private val userRepository = UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())

    private val _signUpResult = MutableLiveData<Result<String>>()
    val signUpResult: LiveData<Result<String>> get() = _signUpResult

    private val _signInResult = MutableLiveData<Result<String>>()
    val signInResult: LiveData<Result<String>> get() = _signInResult

    private val _resetPasswordResult = MutableLiveData<Result<Unit>>()
    val resetPasswordResult: LiveData<Result<Unit>> get() = _resetPasswordResult

    private val _otpVerificationResult = MutableLiveData<Result<Unit>>()
    val otpVerificationResult: LiveData<Result<Unit>> get() = _otpVerificationResult

    private val _passwordUpdateResult = MutableLiveData<Result<Unit>>()
    val passwordUpdateResult: LiveData<Result<Unit>> get() = _passwordUpdateResult

    private val _profileUpdateResult = MutableLiveData<Result<Unit>>()
    val profileUpdateResult: LiveData<Result<Unit>> get() = _profileUpdateResult

    private var tempUser = User()

    fun signUp(context: Context, email: String, password: String, username: String, userType: String) {
        viewModelScope.launch {
            tempUser = User(username = username, email = email, userType = userType)
            val result = authRepository.signUp(email, password, tempUser)
            if (result.isSuccess) {
                result.getOrNull()?.let { uid ->
                    SharedPrefUtil.saveUid(context, uid)
                }
            }
            _signUpResult.value = result
        }
    }

    fun signIn(context: Context, email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            if (result.isSuccess && rememberMe) {
                result.getOrNull()?.let { uid ->
                    SharedPrefUtil.saveUid(context, uid)
                }
            }
            _signInResult.value = result
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordResult.value = authRepository.resetPassword(email)
        }
    }

    fun verifyOtp(context: Context, email: String) {
        viewModelScope.launch {
            val result = authRepository.verifyEmailLink(email)
            if (result.isSuccess) {
                SharedPrefUtil.saveUid(context, authRepository.getCurrentUserId() ?: "")
            }
            _otpVerificationResult.value = result
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _passwordUpdateResult.value = authRepository.updatePassword(newPassword)
        }
    }

    fun updateCraftType(craftType: String) {
        tempUser = tempUser.copy(craftType = craftType)
    }

    fun updateCraftSkill(craftSkill: String) {
        tempUser = tempUser.copy(craftSkill = craftSkill)
    }

    fun updateNationalId(nationalId: String) {
        tempUser = tempUser.copy(nationalId = nationalId)
    }

    fun updateProfileImage(context: Context, imageUrl: String) {
        tempUser = tempUser.copy(profileImageUrl = imageUrl)
        viewModelScope.launch {
            val result = userRepository.saveUser(tempUser)
            if (result.isSuccess) {
                SharedPrefUtil.saveUid(context, authRepository.getCurrentUserId() ?: "")
            }
            _profileUpdateResult.value = result
        }
    }

    fun updateProfile(context: Context, firstName: String, lastName: String, phone: String, location: String, gender: String, birthdate: String) {
        viewModelScope.launch {
            tempUser = tempUser.copy(
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                location = location,
                gender = gender,
                birthdate = birthdate
            )
            val result = userRepository.saveUser(tempUser)
            if (result.isSuccess) {
                SharedPrefUtil.saveUid(context, authRepository.getCurrentUserId() ?: "")
            }
            _profileUpdateResult.value = result
        }
    }

    fun signOut(context: Context) {
        authRepository.signOut()
        SharedPrefUtil.clearUid(context)
    }

    fun isLoggedIn(context: Context): Boolean {
        return SharedPrefUtil.getUid(context) != null && authRepository.getCurrentUserId() != null
    }
}