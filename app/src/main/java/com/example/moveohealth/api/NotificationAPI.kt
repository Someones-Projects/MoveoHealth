package com.example.moveohealth.api

import com.example.moveohealth.constants.Constants.Companion.CONTENT_TYPE
import com.example.moveohealth.constants.Constants.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
            @Body notification: PushNotification
    ): Response<ResponseBody>


}