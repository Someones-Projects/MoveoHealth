package com.example.moveohealth.ui.main

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.model.User
import com.example.moveohealth.model.UserType
import com.example.moveohealth.repository.MainRepository
import com.example.moveohealth.session.SessionManager
import com.example.moveohealth.ui.BaseViewModel
import com.example.moveohealth.ui.DataState
import com.example.moveohealth.ui.Loading
import com.example.moveohealth.ui.main.state.MainStateEvent
import com.example.moveohealth.ui.main.state.MainStateEvent.*
import com.example.moveohealth.ui.main.state.MainViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel
@ViewModelInject
constructor(
    private val mainRepository: MainRepository,
    private val sessionManager: SessionManager,
    @Assisted private val savedStateHandle: SavedStateHandle
):BaseViewModel<MainStateEvent, MainViewState>() {

    private val _userType = MutableLiveData<UserType>()
    val userType: LiveData<UserType>
        get() = _userType

    private val _isFilteredDoctors = MutableLiveData(true)
    val isFilteredDoctors: LiveData<Boolean>
        get() = _isFilteredDoctors


    init {
        _userType.value = sessionManager.cachedUser.value?.userType

        viewModelScope.launch {
            sessionManager.isFilteredDoctors.collect {
                Timber.tag(APP_DEBUG).d("MainViewModel: sessionManager.isSortedDoctors.collect: isSortDoctors.value = $it: ")
                _isFilteredDoctors.value = it
            }
        }
    }

    fun getIsSortDoctors(): Boolean? = isFilteredDoctors.value

    fun toggleSort() {
        sessionManager.toggleSort()
    }
    val doctorList: LiveData<List<User>?> = isFilteredDoctors.switchMap { filter ->
        mainRepository
            .listenAllDoctorsList(filter)
            .distinctUntilChanged()
            .asLiveData()
    }

    fun listenToUserChanges(): Flow<User?> {
        return mainRepository.listenToCurrentUserChanges()
    }


    fun listenPatientWaitingListFlow(): Flow<Pair<User?,List<User>?>> = mainRepository
        .listenDoctorWaitingList().distinctUntilChanged()



    override fun handleStateEvent(stateEvent: MainStateEvent): LiveData<DataState<MainViewState>> {
        return when(stateEvent) {

            is UpdateChangeUserType -> {
                mainRepository.updateCurrentUserType(
                    type = getOtherType()
                ).asLiveData()
            }

            is EnterNewPatientFromQueue -> {
                sessionManager.cachedUser.value!!.userId.let {  doctorId ->
                    mainRepository.setCurrPatientAndRemoveFromWaitingList(
                        patient = stateEvent.patient,
                        doctorId = doctorId
                    ).asLiveData()
                }
            }

            is PatientClickedStartTreatment -> {
                mainRepository.startPatientSessionForDoctor(doctorId = stateEvent.doctorId).asLiveData()
            }

            is PatientClickedAddToWaitList -> {
                mainRepository.addPatientToDoctorWaitList(doctorId = stateEvent.doctorId).asLiveData()
            }

            is PatientClickedDoneTreatment -> {
                mainRepository.setCurrPatientAndRemoveFromWaitingList(
                    patient = stateEvent.nextPatient,
                    doctorId = stateEvent.doctorId
                ).asLiveData()
            }
            is PatientClickedRemoveFromWaitList -> {
                sessionManager.cachedUser.value!!.let {
                    mainRepository.removePatientFromDoctorWaitList(
                        patient = it,
                        doctorId = stateEvent.doctorId
                    ).asLiveData()
                }
            }

            is None -> {
                liveData {
                    emit(
                        DataState<MainViewState>(
                            error = null,
                            loading = Loading(false),
                            success = null
                        )
                    )
                }
            }
        }
    }

    private fun getOtherType(): UserType {
        sessionManager.cachedUser.value!!.userType.let {
            return when (it) {
                UserType.DOCTOR -> UserType.PATIENT
                UserType.PATIENT -> UserType.DOCTOR
            }
        }
    }

    override fun initNewViewState(): MainViewState {
        return MainViewState()
    }

    fun getUsername(): String = sessionManager.cachedUser.value?.username ?: ""

    fun getUserId(): String = sessionManager.cachedUser.value?.userId ?: ""

    fun setUserType(userType: UserType) {
        if (_userType.value != userType) {
            _userType.value = userType
        }
    }


}