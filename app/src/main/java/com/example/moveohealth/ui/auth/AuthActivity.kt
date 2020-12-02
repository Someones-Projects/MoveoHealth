package com.example.moveohealth.ui.auth

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.moveohealth.R
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.constants.Constants.Companion.KEY_INTENT_USER_TYPE
import com.example.moveohealth.databinding.ActivityAuthBinding
import com.example.moveohealth.session.SessionManager
import com.example.moveohealth.ui.BaseActivity
import com.example.moveohealth.ui.main.MainActivity
import com.example.moveohealth.ui.ToolbarInteraction
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity: BaseActivity(), ToolbarInteraction {

    private val viewModel: AuthViewModel by viewModels()

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

    private fun subscribeObservers() {
        viewModel.dataState.observe(this, { dataState ->
            Timber.tag(APP_DEBUG).d("AuthActivity: subscribeObservers: dataState = $dataState")
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
            Timber.tag(APP_DEBUG).d("AuthActivity: subscribeObservers: dataState = $viewState")
            viewState.user?.let {
                sessionManager.login(it)
            }
        })
        sessionManager.cachedUser.observe(this, { user ->
            Timber.tag(APP_DEBUG).e("AuthActivity: subscribeObservers: user = $user")
            user?.let {
                navMainActivity()
            }
        })
    }

    private fun setCollapseToolbar() {
        setSupportActionBar(binding.toolbar)
        val orientation = resources.configuration.orientation
        val isExpand = (orientation == Configuration.ORIENTATION_PORTRAIT)
        binding.appBar.setExpanded(isExpand, false)
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

    private fun navMainActivity() {
        Timber.tag(APP_DEBUG).e("AuthActivity: navMainActivity: ...")
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(KEY_INTENT_USER_TYPE, viewModel.getUserType().name)
        startActivity(intent)
        finish()
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
        supportActionBar?.apply {
            title = text
        }
    }
}