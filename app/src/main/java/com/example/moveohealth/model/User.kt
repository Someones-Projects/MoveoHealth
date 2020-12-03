package com.example.moveohealth.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val userId: String = "", //Document ID is actually the doctor id
    val email: String  = "",
    val username: String  = "No name",
    val userType: UserType = UserType.DOCTOR,
    val currentPatient: User? = null, // relevant only for doctors
    val waitingList: List<User>? = null, // relevant only for doctors
    val imageUrl: String? = null,
    val createdAt: Timestamp ? = null,
    var updatedAt: Timestamp? = null
) : Parcelable {


    companion object {

        const val KEY_USER_ID = "userId"
        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "username"
        const val KEY_USER_TYPE = "userType"
        const val KEY_CURRENT_PATIENT = "currentPatient"
        const val KEY_WAITING_LIST = "waitingList"
        const val KEY_IMAGE_URL = "imageUrl"
        const val KEY_CREATED_AT = "createdAt"
        const val KEY_UPDATED_AT = "updatedAt"

    }

    override fun toString(): String {
        return "User(userId='$userId', email='$email', username='$username', userType=$userType, currentPatient=$currentPatient, waitingList=$waitingList, imageUrl=$imageUrl, createdAt=$createdAt, updatedAt=$updatedAt)"
    }



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (userId != other.userId) return false
        if (email != other.email) return false
        if (username != other.username) return false
        if (userType != other.userType) return false
        if (currentPatient != other.currentPatient) return false
        if (waitingList != other.waitingList) return false
        if (imageUrl != other.imageUrl) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + userType.hashCode()
        result = 31 * result + (currentPatient?.hashCode() ?: 0)
        result = 31 * result + (waitingList?.hashCode() ?: 0)
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        return result
    }



}

enum class UserType {
    DOCTOR,
    PATIENT
}