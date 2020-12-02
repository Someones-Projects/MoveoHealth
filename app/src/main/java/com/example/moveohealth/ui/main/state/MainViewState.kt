package com.example.moveohealth.ui.main.state

import android.net.Uri
import android.os.Parcelable
import com.example.moveohealth.model.User
import kotlinx.android.parcel.Parcelize

@Parcelize
class MainViewState(
    var doctorFields: DoctorScreenFields = DoctorScreenFields(),
    var patientFields: PatientScreenFields = PatientScreenFields()

) : Parcelable {

    @Parcelize
    data class DoctorScreenFields(
        var waitingList: List<User>? = null,
    ): Parcelable

    @Parcelize
    data class PatientScreenFields(
        var username: String? = null,
        var imageUri: Uri? = null
    ): Parcelable
}
