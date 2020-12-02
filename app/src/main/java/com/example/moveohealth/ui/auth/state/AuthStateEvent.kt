package com.example.moveohealth.ui.auth.state

import com.example.moveohealth.model.UserType

sealed class AuthStateEvent{

    data class LoginAttemptEvent(
            val userType: UserType,
            val email: String,
            val password: String
    ): AuthStateEvent()

//    data class LoginSocialEvent(
//            val access_token: String,
//            val socialType: SocialLoginType
//    ): AuthStateEvent()

    data class RegisterAttemptEvent(
        var username: String,
        var userType: UserType,
        val email: String,
        val password: String,
        val confirm_password: String
    ): AuthStateEvent()


    object None : AuthStateEvent()
}