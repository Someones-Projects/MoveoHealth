package com.example.moveohealth.ui.auth

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.moveohealth.R
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.databinding.ActivityAuthBinding
import com.example.moveohealth.ui.BaseActivity
import com.example.moveohealth.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

@AndroidEntryPoint
class AuthActivity: BaseActivity() {

    private val viewModel: AuthViewModel by viewModels()

    private val alreadyNavMainActivity =  AtomicBoolean(false)

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCollapseToolbar()
        onRestoreInstanceState()
        subscribeObservers()
        sessionManager.checkPreviousAuthUser()
    }

    private fun onRestoreInstanceState() {
        supportFragmentManager.findFragmentById(R.id.auth_fragments_container).let { hostFragment ->
            if (hostFragment == null ){
                createNavHost()
            }
        }
    }

    private fun createNavHost() {
        val navHost = NavHostFragment.create(R.navigation.auth_nav_graph)
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.auth_fragments_container,
                navHost,
            )
            .setPrimaryNavigationFragment(navHost)
            .commit()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(this, { dataState ->
            onDataStateChange(dataState)
            dataState.success.let { data ->
                data?.data.let { event ->
                    event?.getContentIfNotHandled().let { viewState ->
                        viewState?.user.let {
                            viewModel.setUserViewState(it)
                        }
                    }
                }
            }
        })
        viewModel.viewState.observe(this, { viewState ->
            viewState.user?.let {
                sessionManager.login(it)
            }
        })
        sessionManager.cachedUser.observe(this, { user ->
            if (user != null) {
                if (user.userId.isNotBlank() && user.email.isNotBlank()) { // state - user logged in
                    showProgressBar(true)
                    navMainActivity()
                } else { //  state - splash while authenticate
                    showProgressBar(true)
                    binding.authFragmentsContainer.visibility = View.INVISIBLE
                    binding.textSplash.visibility = View.VISIBLE
                    binding.textSplash.text = getString(R.string.app_name) + "\nBy Asaf Ben Artzy"
                }
            } else { // state - show login
                showProgressBar(false)
                binding.authFragmentsContainer.visibility = View.VISIBLE
                binding.textSplash.visibility = View.INVISIBLE
            }
        })
    }

    private fun setCollapseToolbar() {
        setSupportActionBar(binding.toolbar)
        val orientation = resources.configuration.orientation
        val isExpand = (orientation == Configuration.ORIENTATION_PORTRAIT)
        binding.appBar.setExpanded(isExpand, false)
    }

    private fun navMainActivity() {
        // check b/c until activity destroy still observe cached user and can call it twice
        if (alreadyNavMainActivity.compareAndSet(false, true)) {
            Timber.tag(APP_DEBUG).e("AuthActivity: navMainActivity: ...")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
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
        Timber.tag(APP_DEBUG).e("AuthActivity: setToolbarTitle: $text")
        binding.toolbarLayout.title = text
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag(APP_DEBUG).e("AuthActivity: onDestroy: ...")
    }
}