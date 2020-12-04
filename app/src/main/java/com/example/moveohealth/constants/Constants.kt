package com.example.moveohealth.constants

import androidx.datastore.preferences.preferencesKey
import com.example.moveohealth.model.User
import com.example.moveohealth.model.UserType

class Constants {

    companion object{

        const val APP_DEBUG = "AppDebug"

//        const val BASE_URL = "https://fcm.googleapis.com"
        const val BASE_URL = "https://wodstalk.com/api/"

        const val SERVER_KEY = "AAAAjrlUKeg:APA91bE0rwh9PIY8Fa_O3AMRDh3UDr0MSEOOTwK6P3Yr-QHGbVOcn1qOkjs7HUkdS8yItkCoIp65frcQWRy0dN6_2Pou6GGJ21KpZ_NQgF8vdQaQ90tsqAsxTib1Ii7u0iygSazolrCy"
        const val CONTENT_TYPE = "application/json"
        const val TOPIC = "/topics/myTopic2"

        const val FIRESTORE_ALL_USERS_KEY: String = "users"

        val IS_FILTERED_DOCTORS_KEY = preferencesKey<Boolean>("IS_SORTED_DOCTORS_KEY")
        val FMC_TOKEN = preferencesKey<String>("FMC_TOKEN")

    }
}