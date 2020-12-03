package com.example.moveohealth.repository

import com.example.moveohealth.constants.Constants
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.model.User
import com.example.moveohealth.model.User.Companion.KEY_USER_TYPE
import com.example.moveohealth.model.UserType
import com.example.moveohealth.ui.DataState
import com.example.moveohealth.ui.Response
import com.example.moveohealth.ui.ResponseType
import com.example.moveohealth.ui.auth.state.AuthViewState
import com.example.moveohealth.ui.auth.state.LoginFields
import com.example.moveohealth.ui.auth.state.RegistrationFields
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import timber.log.Timber


@ExperimentalCoroutinesApi
class AuthRepository
constructor(
    private val auth: FirebaseAuth,
    firestore: FirebaseFirestore
){

    private val usersCollectionRef = firestore.collection(Constants.FIRESTORE_ALL_USERS_KEY)

    fun loginMailAndPassword(
        userType: UserType,
        email: String,
        password: String
    ): Flow<DataState<AuthViewState>> = callbackFlow {

        offer(DataState.loading(isLoading = true))

        val loginFieldErrors = LoginFields(
            login_userType= userType,
            login_email = email,
            login_password = password
        ).isValidForLogin()
        if (loginFieldErrors != LoginFields.LoginError.NONE) {
            cancel(message = loginFieldErrors)
        }

        if (coroutineContext.isActive) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        Timber.tag(APP_DEBUG).e("AuthRepository: loginMailAndPassword: signInWithEmailAndPassword: success --> user = ${firebaseUser?.email}")
                        if (firebaseUser != null) {
                            usersCollectionRef.document(firebaseUser.uid).update(KEY_USER_TYPE, userType)
                            usersCollectionRef.document(firebaseUser.uid).get().addOnCompleteListener { getUserTask ->
                                if (getUserTask.isSuccessful) {
                                    getUserTask.result?.toObject(User::class.java)?.let { user ->
                                        offer(
                                            DataState.success(
                                                AuthViewState(user = user)
                                            )
                                        )
                                        close()
                                    }
                                }
                                cancel("Failed retrieve a user from db")
                            }
                        } else {
                            cancel("Failed to SignUp")
                        }
                    } else {
                        Timber.tag(APP_DEBUG).e("AuthRepository: loginMailAndPassword: createUserWithEmail: failure --> ${task.exception}")
                        val errMsg = task.exception?.message ?: "Failed to SignUp"
                        offer(
                            returnErrorResponse(errMsg, ResponseType.Dialog)
                        )
                        close()
                    }
                }
        }
        awaitClose {
            Timber.tag(APP_DEBUG).d("AuthRepository: loginMailAndPassword: awaitClose - closing flow")
        }
    }.catch {
        // If exception is thrown, emit failed state along with message.
        Timber.tag(APP_DEBUG).e("AuthRepository: loginMailAndPassword: exception: $it")
        emit(returnErrorResponse(it.message.toString(), ResponseType.None))
    }.flowOn(IO)


    fun registerMailAndPassword(
        username: String = "",
        userType: UserType,
        email: String,
        password: String,
        confirmPassword: String
    ): Flow<DataState<AuthViewState>> = callbackFlow {

        offer(DataState.loading(isLoading = true))

        val registrationFieldErrors =
            RegistrationFields(
                registration_username = username,
                registration_userType = userType,
                registration_email = email,
                registration_password = password,
                registration_confirm_password = confirmPassword
            ).isValidForRegistration()
        if (registrationFieldErrors != RegistrationFields.RegistrationErrors.NONE) {
            cancel(message = registrationFieldErrors)
        }

        if (coroutineContext.isActive) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        Timber.tag(APP_DEBUG).e("AuthActivity: register: createUserWithEmail: success --> firebaseUser = $firebaseUser")

                        val newUser = User(
                            userId = firebaseUser!!.uid,
                            username = username,
                            email = email,
                            userType = userType,
                            updatedAt = Timestamp.now(),
                            createdAt = Timestamp.now()
                        ).also { Timber.tag(APP_DEBUG).d("AuthRepository: registerMailAndPassword: newUser = $it") }

                        usersCollectionRef.document(firebaseUser.uid).set(newUser)
                            .addOnCompleteListener { taskSetUser ->
                                if (taskSetUser.isSuccessful) {
                                    offer(
                                        DataState.success(
                                            AuthViewState(user = newUser)
                                        )
                                    )
                                } else {
                                    Timber.tag(APP_DEBUG).e("createUserWithEmail: failure --> ${task.exception}")
                                    val errMsg = taskSetUser.exception?.message ?: "Failed to create user"
                                    offer(
                                        returnErrorResponse(errMsg, ResponseType.Dialog)
                                    )
                                }
                                close() // close channel
                            }

                    } else {
                        // If sign in fails, display a message to the user.
                        Timber.tag(APP_DEBUG).e("createUserWithEmail: failure --> ${task.exception}")
                        val errMsg = task.exception?.message ?: "Failed to SignUp"
                        offer(
                            returnErrorResponse(errMsg, ResponseType.Dialog)
                        )
                        close() // close channel
                    }
                }
        }
        awaitClose {
            Timber.tag(APP_DEBUG).d("AuthRepository: registerMailAndPassword: awaitClose - closing flow")
        }
    }.catch {
        // If exception is thrown, emit failed state along with message.
        Timber.tag(APP_DEBUG).e("AuthRepository: registerMailAndPassword: exception: $it")
        emit(returnErrorResponse(it.message.toString(), ResponseType.None))
    }.flowOn(IO)

    private fun returnErrorResponse(errorMessage: String, responseType: ResponseType): DataState<AuthViewState> {
        return DataState.error(
            Response(
                errorMessage,
                responseType
            )
        )
    }

}