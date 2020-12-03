package com.example.moveohealth.ui.main.state

import com.example.moveohealth.model.User
import com.example.moveohealth.model.UserType

sealed class MainStateEvent{

    data class EnterNewPatientFromQueue(
        val patient: User?,
    ): MainStateEvent()

    data class PatientClickedStartTreatment(
        val doctorId: String,
    ): MainStateEvent()

    data class PatientClickedDoneTreatment(
        val doctorId: String,
        val nextPatient: User?
    ): MainStateEvent()

    data class PatientClickedAddToWaitList(
        val doctorId: String,
    ): MainStateEvent()

    data class PatientClickedRemoveFromWaitList(
        val doctorId: String,
    ): MainStateEvent()

//    data class UpdateChangeUserType(
//        val type: UserType
//    ) : MainStateEvent()


    object UpdateChangeUserType : MainStateEvent()

    object None : MainStateEvent()
}