package com.example.handicraft_graduation_project_2025.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPrefUtil {
    private const val PREF_NAME = "HandiCraftPrefs"
    private const val KEY_UID = "user_uid"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUid(context: Context, uid: String) {
        getPreferences(context).edit().putString(KEY_UID, uid).apply()
    }

    fun getUid(context: Context): String? {
        return getPreferences(context).getString(KEY_UID,null)
    }

    fun clearUid(context: Context) {
        getPreferences(context).edit().remove(KEY_UID).apply()
    }
}