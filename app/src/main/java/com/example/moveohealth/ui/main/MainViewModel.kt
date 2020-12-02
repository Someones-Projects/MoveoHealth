package com.example.moveohealth.ui.main

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.example.moveohealth.model.User
import com.example.moveohealth.repository.MainRepository
import com.example.moveohealth.ui.BaseViewModel
import com.example.moveohealth.ui.DataState
import com.example.moveohealth.ui.Loading
import com.example.moveohealth.ui.main.state.MainStateEvent
import com.example.moveohealth.ui.main.state.MainStateEvent.*
import com.example.moveohealth.ui.main.state.MainViewState
import com.example.moveohealth.ui.main.state.MainViewState.DoctorScreenFields
import kotlinx.coroutines.flow.Flow

class MainViewModel
@ViewModelInject
constructor(
    private val mainRepository: MainRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
):BaseViewModel<MainStateEvent, MainViewState>() {

    init {
        setStateEvent(GetAllDoctorWaitingList)
    }

    override fun handleStateEvent(stateEvent: MainStateEvent): LiveData<DataState<MainViewState>> {
        return when(stateEvent) {
            is RemoveFirstPatientFromQueue -> {
                mainRepository.removePatientFromWaitingList().asLiveData()
            }

            is GetAllDoctorWaitingList -> {
                mainRepository.getDoctorPatientsList().asLiveData()
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

    override fun initNewViewState(): MainViewState {
        return MainViewState()
    }

    fun setDoctorFieldsViewState(doctorFields: DoctorScreenFields) {
        val update = getCurrentViewStateOrNew()
        if (update.doctorFields != doctorFields) {
            update.doctorFields = doctorFields
            setViewState(update)
        }

    }

//    fun getPatientsListenerFlow(): Flow<List<User>?> = mainRepository.getDoctorPatientsList()

}