package com.example.handicraft.data.models

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Collections
import java.util.Date
@Parcelize
data class User(
    var uid: String = "",
    var username: String = "",
    var email: String = "",
    var userType: String = "",
    var craftType: String? = null,
    var craftSkill: String? = null,
    var nationalId: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var phone: String? = null,
    var location: String? = null,
    var gender: String? = null,
    var birthdate: String? = null,
    var profileImageUrl: String? = null,
    var posts: MutableSet<String> = Collections.emptySet(),
    var favorites: MutableSet<String> = Collections.emptySet(),
    var followers: MutableSet<String> = Collections.emptySet(),
    var following: MutableSet<String> = Collections.emptySet(),
    var notificationsEnabled: Boolean = true,
    var blockedUsers: List<String> = Collections.emptyList(),
    @ServerTimestamp val createdAt: Date? = null
):Parcelable
