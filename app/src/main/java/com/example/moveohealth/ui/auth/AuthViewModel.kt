package com.example.moveohealth.ui.auth

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.example.moveohealth.model.UserType
import com.example.moveohealth.repository.AuthRepository
import com.example.moveohealth.ui.BaseViewModel
import com.example.moveohealth.ui.DataState
import com.example.moveohealth.ui.Loading
import com.example.moveohealth.ui.auth.state.AuthStateEvent
import com.example.moveohealth.ui.auth.state.AuthStateEvent.*
import com.example.moveohealth.ui.auth.state.AuthViewState
import com.example.moveohealth.ui.auth.state.LoginFields
import com.example.moveohealth.ui.auth.state.RegistrationFields
import com.google.firebase.auth.FirebaseUser


class AuthViewModel
@ViewModelInject
constructor(
    private val authRepository: AuthRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
): BaseViewModel<AuthStateEvent, AuthViewState>()
{

    private var selectedUserType: UserType = UserType.DOCTOR

    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        when(stateEvent){

            is LoginAttemptEvent -> {
                return authRepository.loginMailAndPassword(
                    userType = stateEvent.userType,
                    email = stateEvent.email,
                    password = stateEvent.password
                ).asLiveData()
            }

            is RegisterAttemptEvent -> {
                return authRepository.registerMailAndPassword(
                    username = stateEvent.username,
                    userType = stateEvent.userType,
                    email = stateEvent.email,
                    password = stateEvent.password,
                    confirmPassword = stateEvent.confirm_password,

                ).asLiveData()
            }

            is None ->{
                return liveData {
                    emit(
                        DataState<AuthViewState>(
                            error = null,
                            loading = Loading(false),
                            success = null
                        )
                    )
                }
            }
        }
    }

    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }

    fun setRegistrationFields(registrationFields: RegistrationFields){
        val update = getCurrentViewStateOrNew()
        if(update.registrationFields == registrationFields){
            return
        }
        update.registrationFields = registrationFields
        setViewState(update)
    }

    fun setLoginFields(loginFields: LoginFields){
        val update = getCurrentViewStateOrNew()
        if(update.loginFields == loginFields){
            return
        }
        update.loginFields = loginFields
        setViewState(update)
    }

    fun getRegistrationEmail(): String {
        return getCurrentViewStateOrNew().registrationFields?.registration_email ?: ""
    }

    fun cancelActiveJobs() {
        setStateEvent(None)
//        authRepository.cancelActiveJobs()
    }

    fun setUserViewState(user: FirebaseUser?) {
        val update = getCurrentViewStateOrNew()
        if (update.user != user) {
            update.user = user
            setViewState(update)
        }
    }

    fun getUserType(): UserType = selectedUserType

    fun setUserType(type: UserType) {
        selectedUserType = type
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}
