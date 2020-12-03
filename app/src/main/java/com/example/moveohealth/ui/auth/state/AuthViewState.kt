package com.example.moveohealth.ui.auth.state

import android.os.Parcelable
import com.example.moveohealth.model.User
import com.example.moveohealth.model.UserType
import com.example.moveohealth.ui.auth.state.RegistrationFields.RegistrationErrors.Companion.MUST_FILL_ALL_FIELDS
import com.example.moveohealth.ui.auth.state.RegistrationFields.RegistrationErrors.Companion.NOT_VALID_EMAIL
import com.example.moveohealth.ui.auth.state.RegistrationFields.RegistrationErrors.Companion.PASSWORD_CONTAINS_6_CHARS
import com.example.moveohealth.ui.auth.state.RegistrationFields.RegistrationErrors.Companion.PASSWORD_DONT_MATCH
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AuthViewState(
        var registrationFields: RegistrationFields? = RegistrationFields(),
        var loginFields: LoginFields? = LoginFields(),
        var user: User? = null
) : Parcelable


@Parcelize
data class RegistrationFields(
    var registration_username: String? = null,
    var registration_userType: UserType = UserType.DOCTOR,
    var registration_email: String? = null,
    var registration_password: String? = null,
    var registration_confirm_password: String? = null
) : Parcelable {

    fun isValidForRegistration(): String{

        // not suppose to get here.. deal from registration fragment
        if (registration_email.isNullOrBlank()
                ||  registration_password.isNullOrBlank()
                ||  registration_confirm_password.isNullOrBlank()
        ) {
            return MUST_FILL_ALL_FIELDS
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(registration_email!!).matches()) {
            return NOT_VALID_EMAIL
        }

        val passwordLength = registration_password!!.length
        if (passwordLength < 6) {
            return PASSWORD_CONTAINS_6_CHARS
        }

//        if (registration_password!!.takeWhile{it.isDigit()}.length == passwordLength) {
//            return PASSWORD_ENTIRELY_NUMERIC
//        }

        if (!registration_password.equals(registration_confirm_password)) {
            return PASSWORD_DONT_MATCH
        }

        return RegistrationErrors.NONE
    }

    class RegistrationErrors {
        companion object{
            const val MUST_FILL_ALL_FIELDS = "All fields are required."
            const val NOT_VALID_EMAIL = "Not a valid email"
            const val PASSWORD_DONT_MATCH = "Passwords don't match."
            const val PASSWORD_CONTAINS_6_CHARS = "Must contain at least 6 characters"
//            const val PASSWORD_ENTIRELY_NUMERIC = "Password can't be entirely numeric"
            const val NONE = "None"
        }
    }
}

@Parcelize
data class LoginFields(
    var login_userType: UserType = UserType.DOCTOR,
    var login_email: String? = null,
    var login_password: String? = null
) : Parcelable {

    fun isValidForLogin(): String{

        // not suppose to get here.. deal from login fragment
        if(login_email.isNullOrEmpty() || login_password.isNullOrEmpty()){
            return LoginError.MUST_FILL_ALL_FIELDS
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(login_email!!).matches()) {
            return LoginError.NOT_VALID_EMAIL
        }

        return LoginError.NONE
    }

    override fun toString(): String {
        return "LoginState(email=$login_email, password=$login_password)"
    }


    class LoginError {

        companion object{
            const val MUST_FILL_ALL_FIELDS = "All fields are required."
            const val NOT_VALID_EMAIL = "Not a valid email"
            const val NONE = "None"
        }
    }
}