package com.example.handicraft_graduation_project_2025.ui.viewmodels



import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil

class SplashViewModel : ViewModel() {
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    fun checkUserLogin(context: Context) {
        _isLoggedIn.value = SharedPrefUtil.getUid(context) != null
    }
}