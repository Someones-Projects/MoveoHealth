package com.example.moveohealth.ui.main.state

sealed class MainStateEvent{

    data class RemoveFirstPatientFromQueue(
        val userId: String,
    ): MainStateEvent()

//    object RemoveFirstPatientFromQueue : MainStateEvent()

    object GetAllDoctorWaitingList : MainStateEvent()

    object None : MainStateEvent()
}