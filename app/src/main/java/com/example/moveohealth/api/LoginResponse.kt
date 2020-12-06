package com.example.moveohealth.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class LoginResponse(

        @SerializedName("response")
        @Expose
        var response: String,

        @SerializedName("error_message")
        @Expose
        var errorMessage: String?,

        @SerializedName("token")
        @Expose
        var token: String,

        @SerializedName("pk")
        @Expose
        var pk: Int,

        @SerializedName("email")
        @Expose
        var email: String,

        @SerializedName("username")
        @Expose
        var username: String,

        @SerializedName("gender")
        @Expose
        var gender: String?,

        @SerializedName("age")
        @Expose
        var age: String?,

        @SerializedName("height")
        @Expose
        var height: Float?,

        @SerializedName("weight")
        @Expose
        var weight: Float?,

        @SerializedName("image")
        @Expose
        var image: String?,

        @SerializedName("is_premium")
        @Expose
        var isPremium: Boolean,

        )