package com.example.moveohealth.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.moveohealth.R
import com.example.moveohealth.constants.Constants
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.constants.Constants.Companion.KEY_INTENT_USER_TYPE
import com.example.moveohealth.databinding.ActivityMainBinding
import com.example.moveohealth.model.UserType
import com.example.moveohealth.ui.BaseActivity
import com.example.moveohealth.ui.auth.AuthActivity
import com.example.moveohealth.ui.displayToast
import com.example.moveohealth.ui.main.doctor.DoctorFragment
import com.example.moveohealth.ui.main.state.MainStateEvent.RemoveFirstPatientFromQueue
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent.getStringExtra(KEY_INTENT_USER_TYPE)?.also {
            Timber.tag(APP_DEBUG).e("MainActivity: onCreate: userType = ${UserType.valueOf(it)}")
            displayToast("${UserType.valueOf(it)}")
        }
        restoreSession(savedInstanceState)
        setupToolbar()
        setClickListeners()
        onRestoreInstanceState()
        subscribeObservers()
    }

    private fun setClickListeners() {
        binding.apply {
            fab.setOnClickListener {
                sessionManager.logout()
            }
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

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbarLayout.title = title

    }

    private fun onRestoreInstanceState() {
        supportFragmentManager.findFragmentById(R.id.main_fragments_container).let { hostFragment ->
            if (hostFragment == null ){
                createNavHost()
            }
        }
    }

    private fun createNavHost() {
        val navHost = NavHostFragment.create(R.navigation.doctor_nav_graph)
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.main_fragments_container,
                navHost,
            )
            .setPrimaryNavigationFragment(navHost)
            .commit()
    }


    private fun subscribeObservers() {
        sessionManager.cachedUser.observe(this, { user ->
            if (user == null) {
                navAuthActivity()
            }
        })
        viewModel.dataState.observe(this, { dataState ->
            onDataStateChange(dataState)
        })
    }

    private fun navAuthActivity() {
        Timber.tag(APP_DEBUG).e("MainActivity: navAuthActivity: ...")
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun restoreSession(savedInstanceState: Bundle?) {
        savedInstanceState?.get(FIREBASE_USER_BUNDLE_KEY)?.let{ user ->
            sessionManager.setCachedUserValue(user as FirebaseUser)
        }
    }

    // handle process death
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // save auth token
        outState.putParcelable(FIREBASE_USER_BUNDLE_KEY, sessionManager.cachedUser.value)
    }

    override fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility =
            if (show) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    fun showFab (show: Boolean) {
        if (show) {
            binding.fabExtendedGetNextPatient.show()
        } else {
            binding.fabExtendedGetNextPatient.hide()
        }

    }


    companion object {
        const val NOTES_COLLECTION = "notes"
        const val KEY_TITLE = "title"
        const val KEY_DESCRIPTION = "description"

        const val FIREBASE_USER_BUNDLE_KEY = "firebase user restore key"
    }
}