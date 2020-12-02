package com.example.moveohealth.session

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.auth.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionManager
@Inject
constructor(
    @ApplicationContext val context: Context,
    private val auth: FirebaseAuth
) {

    private val _cachedUser = MutableLiveData<FirebaseUser?>()
    val cachedUser: LiveData<FirebaseUser?>
        get() = _cachedUser

    fun checkPreviousAuthUser() {
        auth.currentUser.let { user ->
            Timber.tag(APP_DEBUG).e("AuthActivity: checkPreviousAuthUser: firebaseAuth.currentUser = $user")
            user?.let{ setCachedUserValue(it) }
        }
    }

    fun login(newValue: FirebaseUser) {
        setCachedUserValue(newValue)
    }

    fun logout() {
        Timber.tag(APP_DEBUG).d("SessionManager: logout..")
        try {
            auth.signOut()
        } catch (e: Exception) {
            Timber.tag(APP_DEBUG).e("SessionManager: logout: ${e.message}")
        } finally {
            setCachedUserValue(null)
        }
    }

    fun setCachedUserValue(newValue: FirebaseUser?) {
        CoroutineScope(Dispatchers.Main).launch {
            if (_cachedUser.value != newValue) {
                _cachedUser.value = newValue
            }
        }
    }
}