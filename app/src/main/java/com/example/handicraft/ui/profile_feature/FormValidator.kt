package com.example.handicraft_graduation_project_2025.ui.profile_feature

object FormValidator {

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isNotEmpty(value: String): Boolean {
        return value.trim().isNotEmpty()
    }

    fun validateProfileForm(
        firstName: String,
        lastName: String,
        email: String,
        phone: String
    ): String? {
        return when {
            !isNotEmpty(firstName) -> "First name is required"
            !isNotEmpty(lastName) -> "Last name is required"
            !isValidEmail(email) -> "Invalid email format"
            !isNotEmpty(phone) -> "Phone number is required"
            else -> null
        }
    }
}
