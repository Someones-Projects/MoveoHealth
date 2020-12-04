package com.example.moveohealth.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.moveohealth.R
import com.example.moveohealth.api.NotificationAPI
import com.example.moveohealth.api.PushNotification
import com.example.moveohealth.api.SyncWodRequest
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.constants.Constants.Companion.TOPIC
import com.example.moveohealth.databinding.ActivityMainBinding
import com.example.moveohealth.model.NotificationData
import com.example.moveohealth.model.User
import com.example.moveohealth.model.UserType
import com.example.moveohealth.ui.BaseActivity
import com.example.moveohealth.ui.auth.AuthActivity
import com.example.moveohealth.ui.displayToast
import com.example.moveohealth.ui.main.doctor.DoctorFragment
import com.example.moveohealth.ui.main.state.MainStateEvent
import com.example.moveohealth.ui.main.state.MainStateEvent.*
import com.example.moveohealth.util.RetrofitInstance
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject
    lateinit var notificationAPI: NotificationAPI

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()
    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        restoreSession(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        setClickListeners()
        subscribeObservers()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.tag(APP_DEBUG).e("AuthActivity: onCreate: Fetching FCM registration token failed - ${task.exception}")
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = "FCM token - $token"
            Timber.tag(APP_DEBUG).e("AuthActivity: onCreate: msg - $msg")
//            displayToast(msg)
        })
        Firebase.messaging.subscribeToTopic(TOPIC)
            .addOnCompleteListener { task ->
                var msg = "Subscribe to $TOPIC"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                // Log and toast
                Timber.tag(APP_DEBUG).e("AuthActivity: onCreate: subscribeToTopic: msg - $msg")
            }
    }

    override fun onStart() {
        super.onStart()
        job = lifecycleScope.launch {
            viewModel.listenToUserChanges().collectLatest { user ->
                sessionManager.setCachedUserValue(user)
            }
        }
    }

    private fun restoreSession(savedInstanceState: Bundle?) {
        savedInstanceState?.get(FIREBASE_USER_ID_BUNDLE_KEY)?.let{ userId ->
            sessionManager.setCachedUserValue(userId as User)
        }
    }

    private fun setClickListeners() {
        binding.apply {
            fabExtendedGetNextPatient.setOnClickListener {
                sendNotification()
//                val fragments = supportFragmentManager
//                    .findFragmentById(R.id.main_fragments_container)
//                    ?.childFragmentManager
//                    ?.fragments
//                fragments?.forEach { fragment ->
//                    when (fragment) {
//                        is DoctorFragment -> fragment.handleNextPatientClicked()
//                    }
//                }
            }
        }
    }

    private fun subscribeObservers() {
        sessionManager.cachedUser.distinctUntilChanged().observe(this, { user ->
            if (user == null) {
                navAuthActivity()
            } else {
                viewModel.setUserType(user.userType)
            }
        })
        viewModel.userType.observe(this, { userType ->
            userType?.let {
                when (it) {
                    UserType.DOCTOR -> {
                        supportFragmentManager.findFragmentById(R.id.doctorFragment)
                            ?: createNavHost(R.navigation.doctor_nav_graph)
                    }
                    UserType.PATIENT -> {
                        supportFragmentManager.findFragmentById(R.id.doctorFragment)
                            ?: createNavHost(R.navigation.patient_nav_graph)
                    }
                }
            }
        })
        viewModel.dataState.observe(this, { dataState ->
            onDataStateChange(dataState)
        })
    }

    private fun sendNotification() {
        Timber.tag(APP_DEBUG).e("MainActivity: sendNotification: ...")

        val notification = PushNotification(
//            to = "c7gwkDsaSLOmOB6nAoZ8DA:APA91bGVHW1fUbZg9JewZ0NNmDoTS4R_4yZ6-U1sztkXF14r_SFykhH2c_zFC5Wf3VlBjpKIMRxIi5iDpwPXQee3ZoVbKRRoGKxSKkFiTmiuFIk8jIYH_jAZEA12df8i8CUWYrVfuKv3"
            to = TOPIC
            ,
            data = NotificationData(
                title = "Title",
                message = "Body message.."
            )
        )


        val msg = RemoteMessage
            .Builder("String")
            .build()


        val response = FirebaseMessaging.getInstance().send(msg)
        Timber.tag(APP_DEBUG).e("MainActivity: sendNotification: response= $response.")

//        try {
//            Timber.tag(APP_DEBUG).e("MainActivity: sendNotification: postNotification(notification)")
////            val response = RetrofitInstance.api.postNotification(notification)
//            val response = notificationAPI.syncWodList(
//                authorization = "notification",
//                requestBody = SyncWodRequest(emptyList())
//            ).execute().also {
//                Timber.tag(APP_DEBUG).e("MainActivity: sendNotification: response = $it")
//            }
//            if(response.isSuccessful) {
//                Timber.tag(APP_DEBUG).d(
//                    "MainRepository: sendNotification: Response: ${
//                        Gson().toJson(
//                            response
//                        )
//                    }"
//                )
//            } else {
//                Timber.tag(APP_DEBUG).e(
//                    "MainRepository: sendNotification: error = ${
//                        response.errorBody().toString()
//                    }"
//                )
//            }
//        } catch (e: Exception) {
//            Timber.tag(APP_DEBUG).e("sendNotification: exception = ${e.message}")
//        }
    }

    private fun createNavHost(navGraphId: Int) {
        Timber.tag(APP_DEBUG).d("MainActivity: createNavHost: ..")
        val navHost = NavHostFragment.create(navGraphId)
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.main_fragments_container,
                navHost,
            )
            .setPrimaryNavigationFragment(navHost)
            .commit()
    }

    private fun navAuthActivity() {
        Timber.tag(APP_DEBUG).e("MainActivity: navAuthActivity: ...")
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_switch_user_type -> {
                viewModel.setStateEvent(UpdateChangeUserType)
                true
            }
            R.id.menu_item_logout -> {
                sessionManager.logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // handle process death
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // save auth token
        outState.putParcelable(FIREBASE_USER_ID_BUNDLE_KEY, sessionManager.cachedUser.value)
    }

    override fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility =
            if (show) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    override fun setToolbarTitle(text: String?) {
        binding.toolbarLayout.title = text
    }

    fun showFab (show: Boolean) {
        if (show) {
            binding.fabExtendedGetNextPatient.show()
        } else {
            binding.fabExtendedGetNextPatient.hide()
        }

    }

    override fun onStop() {
        job.cancel()
        super.onStop()
    }


    companion object {
        const val FIREBASE_USER_ID_BUNDLE_KEY = "firebase user restore key"
    }
}