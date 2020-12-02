package com.example.moveohealth.model

import android.os.Parcelable
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.getField
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

@Parcelize
data class User(
    val userId: String, //Document ID is actually the doctor id
    val username: String,
    val userType: UserType,
    val patients: List<User>? = null,
    val imageUrl: String? = null,
    var updatedAt: Timestamp,
    var createdAt: Timestamp
) : Parcelable {


    companion object {
        fun DocumentSnapshot.toUser(): User? {
            return try {
                val username = getString("username")!!
                val userType = getString("userType")!!
                val imageUrl = getString("profile_image")!!
                val updatedAt = getTimestamp("updatedAt")!!
                val createdAt = getTimestamp("createdAt")!!
                val patients = getField<List<User>>("patients")!!
                User(
                    userId = id,
                    username = username,
                    userType = UserType.valueOf(userType),
                    patients = patients,
                    imageUrl = imageUrl,
                    updatedAt = updatedAt,
                    createdAt = createdAt
                )
            } catch (e: Exception) {
                Timber.tag(APP_DEBUG).e("Doctor: toUser: Error converting user profile - $e")
                FirebaseCrashlytics.getInstance().log("Error converting user profile")
                FirebaseCrashlytics.getInstance().setCustomKey("userId", id)
                FirebaseCrashlytics.getInstance().recordException(e)
                null
            }
        }
    }

    override fun toString(): String {
        return "User(userId='$userId', username='$username', userType=$userType, patients=$patients, imageUrl=$imageUrl, updatedAt=$updatedAt, createdAt=$createdAt)"
    }
}

enum class UserType {
    DOCTOR,
    PATIENT
}