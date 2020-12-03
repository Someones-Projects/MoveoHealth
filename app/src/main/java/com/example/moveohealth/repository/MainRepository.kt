package com.example.moveohealth.repository

import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.constants.Constants.Companion.FIRESTORE_ALL_USERS_KEY
import com.example.moveohealth.model.User
import com.example.moveohealth.model.User.Companion.KEY_CURRENT_PATIENT
import com.example.moveohealth.model.User.Companion.KEY_USER_TYPE
import com.example.moveohealth.model.User.Companion.KEY_WAITING_LIST
import com.example.moveohealth.model.UserType
import com.example.moveohealth.session.SessionManager
import com.example.moveohealth.ui.DataState
import com.example.moveohealth.ui.Response
import com.example.moveohealth.ui.ResponseType
import com.example.moveohealth.ui.main.state.MainViewState
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MainRepository
constructor(
    firestore: FirebaseFirestore,
    private val sessionManager: SessionManager
) {

    private val userSession = sessionManager.cachedUser.value!!

    private val allUsersCollectionRef = firestore.collection(FIRESTORE_ALL_USERS_KEY)
    private val currUserDocumentRef = allUsersCollectionRef.document(userSession.userId)


    fun listenToCurrentUserChanges(): Flow<User?> =  callbackFlow {
        Timber.tag(APP_DEBUG).e("MainRepository: listenToCurrentUserChanges: callbackFlow...")
        val listenerRegistration = currUserDocumentRef
            .addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    cancel(
                        message = "Error fetching doctor's waiting list",
                        cause = firebaseFirestoreException
                    )
                    return@addSnapshotListener
                }
                val user = documentSnapshot?.toObject(User::class.java)
                offer(user)
            }
        awaitClose {
            Timber.tag(APP_DEBUG).e("MainRepository: getDoctorPatientsList: Cancelling patientsList listener")
            listenerRegistration.remove()
        }
    }.catch {
        Timber.tag(APP_DEBUG).e("MainRepository: getDoctorPatientsList: Exception = $it")
        emit(null)
    }.flowOn(IO)

    fun listenDoctorWaitingList(): Flow<Pair<User?, List<User>?>> =  callbackFlow {
        Timber.tag(APP_DEBUG).e("MainRepository: getDoctorPatientsList: callbackFlow...")
        val listenerRegistration = currUserDocumentRef
            .addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    cancel(
                        message = "Error fetching doctor's waiting list",
                        cause = firebaseFirestoreException
                    )
                    return@addSnapshotListener
                }
                documentSnapshot?.toObject(User::class.java)?.let {
                    offer(Pair(it.currentPatient, it.waitingList))
                }
            }
        awaitClose {
            Timber.tag(APP_DEBUG).e("MainRepository: getDoctorPatientsList: Cancelling patientsList listener")
            listenerRegistration.remove()
        }
    }.catch {
        Timber.tag(APP_DEBUG).e("MainRepository: getDoctorPatientsList: Exception = $it")
        emit(Pair(null, emptyList()))
    }.flowOn(IO)


    fun listenAllDoctorsList(
        sortByAvailable: Boolean
    ): Flow<List<User>?> = callbackFlow {
        Timber.tag(APP_DEBUG).e("MainRepository: listenAllDoctorsList: callbackFlow...")
        Timber.tag(APP_DEBUG).d("MainRepository: listenAllDoctorsList: sortByAvailable = $sortByAvailable")
        val listenerRegistration = allUsersCollectionRef
            .whereEqualTo(KEY_USER_TYPE, UserType.DOCTOR)
            .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    cancel(
                        message = "Error fetching doctor's waiting list",
                        cause = firebaseFirestoreException
                    )
                    return@addSnapshotListener
                }
                val doctors = querySnapshot?.toObjects(User::class.java)
                Timber.tag(APP_DEBUG).d("MainRepository: listenAllDoctorsList: doctors  = $doctors")
                if (sortByAvailable) {
                    doctors?.sortBy { it.waitingList?.size }
                    doctors?.sortBy { it.currentPatient != null }
                    Timber.tag(APP_DEBUG).d("MainRepository: listenAllDoctorsList: doctors sorted = $doctors")
                }
                offer(doctors)
            }
        awaitClose {
            Timber.tag(APP_DEBUG).e("MainRepository: listenAllDoctorsList: Cancelling doctorList listener")
            listenerRegistration.remove()
        }
    }.catch {
        Timber.tag(APP_DEBUG).e("MainRepository: listenAllDoctorsList: Exception = $it")
        emit(emptyList())
    }.flowOn(IO)


    fun removeFromWaitingListAndSetCurrPatient(
        patient: User?,
        doctorId: String
    ): Flow<DataState<MainViewState>> = flow<DataState<MainViewState>> {
        Timber.tag(APP_DEBUG).e("MainRepository: removePatientFromWaitingList: ...")
        emit(DataState.loading(isLoading = true))
        val doctorDocumentRef = allUsersCollectionRef.document(doctorId)
        doctorDocumentRef.update(KEY_CURRENT_PATIENT, patient).await()
        patient?.let { // if patient is null that mean no more waiting
            doctorDocumentRef.update(KEY_WAITING_LIST, FieldValue.arrayRemove(patient)).await()
        }
        emit(DataState.success())
    }.catch {
        Timber.tag(APP_DEBUG).e("MainRepository: removeFromWaitingListAndSetCurrPatient: Exception = $it")
        emit(DataState.error(Response(it.message.toString(), ResponseType.Dialog)))
    }.flowOn(IO)


    fun startPatientSessionForDoctor(
        doctorId: String
    ): Flow<DataState<MainViewState>> = flow<DataState<MainViewState>> {
        emit(DataState.loading(isLoading = true))
        allUsersCollectionRef.document(doctorId).update(
            KEY_CURRENT_PATIENT,
            sessionManager.cachedUser.value
        ).await()
        emit(DataState.success())
    }.catch {
        Timber.tag(APP_DEBUG).e("MainRepository: startPatientSessionForDoctor: Exception = $it")
        emit(DataState.error(Response(it.message.toString(), ResponseType.Dialog)))
    }.flowOn(IO)



    fun updateCurrentUserType(
        type: UserType
    ): Flow<DataState<MainViewState>> = flow<DataState<MainViewState>> {
        emit(DataState.loading(isLoading = true))
        currUserDocumentRef.update(KEY_USER_TYPE, type).await()
        emit(DataState.success(
            data = MainViewState()
        ))
    }.catch {
        Timber.tag(APP_DEBUG).e("MainRepository: updateCurrentUserType: Exception = $it")
        emit(DataState.error(Response(it.message.toString(), ResponseType.Dialog)))
    }.flowOn(IO)



    fun addPatientToDoctorWaitList(doctorId: String): Flow<DataState<MainViewState>> {
        return flow<DataState<MainViewState>> {
            Timber.tag(APP_DEBUG).e("MainRepository: addPatientToDoctorWaitList: ...")
            emit(DataState.loading(isLoading = true))
            allUsersCollectionRef
                .document(doctorId)
                .update(KEY_WAITING_LIST, FieldValue.arrayUnion(userSession))
                .await()
            emit(DataState.success())
        }.catch {
            Timber.tag(APP_DEBUG).e("MainRepository: addPatientToDoctorWaitList: Exception = $it")
            emit(DataState.error(Response(it.message.toString(), ResponseType.Dialog)))
        }.flowOn(IO)
    }

    fun removePatientFromDoctorWaitList(
        patient: User,
        doctorId: String
    ): Flow<DataState<MainViewState>> = flow<DataState<MainViewState>> {
        Timber.tag(APP_DEBUG).e("MainRepository: removePatientFromDoctorWaitList: ...")
        emit(DataState.loading(isLoading = true))
        val doctorDocumentRef = allUsersCollectionRef.document(doctorId)
        doctorDocumentRef.update(KEY_WAITING_LIST, FieldValue.arrayRemove(patient)).await()
        emit(DataState.success())
    }.catch {
        Timber.tag(APP_DEBUG).e("MainRepository: removePatientFromDoctorWaitList: Exception = $it")
        emit(DataState.error(Response(it.message.toString(), ResponseType.Dialog)))
    }.flowOn(IO)


}