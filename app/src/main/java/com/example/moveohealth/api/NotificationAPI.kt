package com.example.moveohealth.api

import androidx.lifecycle.LiveData
import com.example.moveohealth.constants.Constants.Companion.CONTENT_TYPE
import com.example.moveohealth.constants.Constants.Companion.SERVER_KEY
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Singleton

@Singleton
interface NotificationAPI {

//    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
//    @POST("fcm/send")
//    suspend fun postNotification(
//        @Body notification: PushNotification
//    ): Response<ResponseBody>

    @POST("wod/synclist")
    fun syncWodList(
        @Header("Authorization") authorization: String,
        @Body requestBody: SyncWodRequest
    ): Call<SyncResultsResponse?>
}

data class SyncWodRequest (

    @SerializedName("wodList")
    @Expose
    var wodList: List<String>
) {
    override fun toString(): String {
        return "SyncWodRequest(wodList=$wodList)"
    }
}


class SyncResultsResponse (

    @SerializedName("results")
    @Expose
    var results: List<SingleSyncResponse>

) {

    override fun toString(): String {
        return "SyncResultsResponse(results=$results)"
    }
}

class SingleSyncResponse (

    @SerializedName("local_db_obj_pk")
    @Expose
    var local_db_obj_pk: Int,

    @SerializedName("server_obj_pk")
    @Expose
    var server_obj_pk: Int,

    @SerializedName("response")
    @Expose
    var response: String

) {

    override fun toString(): String {
        return "SingleSyncResponse(local_db_wod_pk=$local_db_obj_pk," +
                " server_wod_pk=$server_obj_pk," +
                " response='$response')"
    }
}

