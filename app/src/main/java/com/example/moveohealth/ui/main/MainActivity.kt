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
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.constants.Constants.Companion.TOPIC
import com.example.moveohealth.databinding.ActivityMainBinding
import com.example.moveohealth.model.User
import com.example.moveohealth.model.UserType
import com.example.moveohealth.ui.BaseActivity
import com.example.moveohealth.ui.auth.AuthActivity
import com.example.moveohealth.ui.main.doctor.DoctorFragment
import com.example.moveohealth.ui.main.state.MainStateEvent.UpdateChangeUserType
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : BaseActivity() {

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

        // in order to send to specific device we need to send it to token and not topic.
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
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
                val fragments = supportFragmentManager
                    .findFragmentById(R.id.main_fragments_container)
                    ?.childFragmentManager
                    ?.fragments
                fragments?.forEach { fragment ->
                    when (fragment) {
                        is DoctorFragment -> fragment.handleNextPatientClicked()
                    }
                }
            }
        }
    }

    private fun subscribeObservers() {
        sessionManager.cachedUser.distinctUntilChanged().observe(this, { user ->
            if (user == null) {
                Timber.tag(APP_DEBUG).e("MainActivity: subscribeObservers:  user is null!!!")
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

    override fun onDestroy() {
        Timber.tag(APP_DEBUG).d("MainActivity: onDestroy: ")
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC)
        super.onDestroy()
    }


    companion object {
        const val FIREBASE_USER_ID_BUNDLE_KEY = "firebase user restore key"
    }
}