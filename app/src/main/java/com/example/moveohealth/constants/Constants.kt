package com.example.moveohealth.constants

import androidx.datastore.preferences.preferencesKey
import com.example.moveohealth.model.User
import com.example.moveohealth.model.UserType

class Constants {

    companion object{
        const val APP_DEBUG = "AppDebug"

        const val FIRESTORE_ALL_USERS_KEY: String = "users"

        val IS_SORTED_DOCTORS_KEY = preferencesKey<Boolean>("IS_SORTED_DOCTORS_KEY")


    }
}