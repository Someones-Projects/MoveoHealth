package com.example.moveohealth.session

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.constants.Constants.Companion.FIRESTORE_ALL_USERS_KEY
import com.example.moveohealth.constants.Constants.Companion.IS_SORTED_DOCTORS_KEY
import com.example.moveohealth.model.User
import com.example.moveohealth.model.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionManager
@Inject
constructor(
    @ApplicationContext val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val dataStore: DataStore<Preferences>,
) {

    private val _cachedUser = MutableLiveData<User?>()
    val cachedUser: LiveData<User?>
        get() = _cachedUser




    fun checkPreviousAuthUser() {
        auth.currentUser.let { firebaseUser ->
            Timber.tag(APP_DEBUG).e("AuthActivity: checkPreviousAuthUser: firebaseAuth.currentUser = $firebaseUser")
            if (firebaseUser != null) {
                firestore.collection(FIRESTORE_ALL_USERS_KEY).document(firebaseUser.uid).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.toObject(User::class.java)?.let {
                                setCachedUserValue(it)
                            }
                        }
                    }
            }
        }
    }

    fun login(newUser: User, userType:UserType) {
        setCachedUserValue(newUser)
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

    fun setCachedUserValue(newUser: User?) {
        CoroutineScope(Dispatchers.Main).launch {
            if (_cachedUser.value != newUser) {
                _cachedUser.value = newUser
            }
        }
    }

    val isSortedDoctors: Flow<Boolean> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) emit(emptyPreferences())
            else throw exception // "If a different type of exception was thrown, prefer re-throwing it."
        }.map { preferences ->
            preferences[IS_SORTED_DOCTORS_KEY] ?: true
        }


    fun toggleSort() {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                val sorted = preferences[IS_SORTED_DOCTORS_KEY] ?: true
                Timber.tag(APP_DEBUG).d("SessionManager: toggleSort: setting sorted to ${!sorted}")
                preferences[IS_SORTED_DOCTORS_KEY] = !sorted
            }
        }
    }

    fun setSortPref(sorted: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                preferences[IS_SORTED_DOCTORS_KEY]?.let {
                    Timber.tag(APP_DEBUG).d("SessionManager: toggleSort: setting sorted to ${!it}")
                    preferences[IS_SORTED_DOCTORS_KEY] = !it
                }
            }
        }
    }

}