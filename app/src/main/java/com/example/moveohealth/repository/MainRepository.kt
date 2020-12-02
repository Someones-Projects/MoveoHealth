package com.example.moveohealth.repository

import com.example.moveohealth.constants.Constants
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.constants.Constants.Companion.FIRESTORE_USER
import com.example.moveohealth.model.User
import com.example.moveohealth.model.User.Companion.toUser
import com.example.moveohealth.session.SessionManager
import com.example.moveohealth.ui.DataState
import com.example.moveohealth.ui.Response
import com.example.moveohealth.ui.ResponseType
import com.example.moveohealth.ui.main.state.MainViewState
import com.example.moveohealth.ui.main.state.MainViewState.DoctorScreenFields
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

class MainRepository
constructor(
    firestore: FirebaseFirestore,
    sessionManager: SessionManager
) {

    private val usersCollectionRef = firestore.collection(FIRESTORE_USER)
    private val waitingListCollectionRef = usersCollectionRef
        .document(sessionManager.cachedUser.value!!.uid)
        .collection("waiting patients")


    fun getDoctorPatientsList(): Flow<DataState<MainViewState>> =  callbackFlow {
        Timber.tag(APP_DEBUG).e("MainRepository: getDoctorPatientsList: callbackFlow...")
        offer(DataState.loading(isLoading = true))
        val listenerRegistration = waitingListCollectionRef
            .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    cancel(
                        message = "Error fetching doctor's waiting list",
                        cause = firebaseFirestoreException
                    )
                    return@addSnapshotListener
                }
                val map = querySnapshot?.documents?.mapNotNull { it.toUser() }.also {
                    Timber.tag(APP_DEBUG).e("MainViewModel: getPatientsDataFlow: map = $it")
                }
                offer(
                    DataState.success(
                        MainViewState(
                            doctorFields = DoctorScreenFields(map))
                    )
                )
//                val list = ArrayList<User>().apply {
//                    add(User("1234", "Asi"))
//                    add(User("324", "Siso"))
//                    add(User("1234324", "Jojo"))
//                    add(User("34324", "Shimon"))
//                    add(User("113441", "Momo"))
//                }
//                offer(
//                    DataState.success(
//                        MainViewState(
//                            doctorFields = DoctorScreenFields(list))
//                    )
//                )
            }
        awaitClose {
            Timber.tag(APP_DEBUG).e("MainViewModel: getPatientsDataFlow: Cancelling patientsList listener")
            listenerRegistration.remove()
        }
    }.catch {
        Timber.tag(APP_DEBUG).e("MainViewModel: getPatientsDataFlow: Exception = $it")
        emit(DataState.error(Response(it.message, ResponseType.Toast)))
    }.flowOn(Dispatchers.IO)


    fun removePatientFromWaitingList(): Flow<DataState<MainViewState>> = flow<DataState<MainViewState>> {
        Timber.tag(APP_DEBUG).e("MainRepository: removePatientFromWaitingList: ...")
        emit(DataState.loading(isLoading = true))
        delay(2000)
        val listenerRegistration = waitingListCollectionRef

        emit(DataState.success())
    }.catch {
        Timber.tag(APP_DEBUG).e("MainViewModel: getPatientsDataFlow: Exception = $it")
    }.flowOn(Dispatchers.IO)
}