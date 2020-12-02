package com.example.moveohealth.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.moveohealth.R
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.ui.DataStateChangeListener
import com.example.moveohealth.ui.ToolbarInteraction
import com.example.moveohealth.ui.UICommunicationListener
import timber.log.Timber

abstract class BaseAuthFragment
constructor(
    @LayoutRes
    private val layoutRes: Int
): Fragment(layoutRes) {

    lateinit var stateChangeListener: DataStateChangeListener
    lateinit var uiCommunicationListener: UICommunicationListener
    lateinit var toolbarInteraction: ToolbarInteraction

    val viewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(activity as AuthActivity)
        setHasOptionsMenu(true)
        uiCommunicationListener.hideSoftKeyboard()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBarWithNavController(activity: AuthActivity) {
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loginFragment,)
        )
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Timber.tag(APP_DEBUG)
                .e("BaseAccountFragment: onAttach: $context must implement DataStateChangeListener")
        }
        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Timber.tag(APP_DEBUG)
                .e("BaseAccountFragment: onAttach: $context must implement UICommunicationListener")
        }
        try {
            toolbarInteraction = context as ToolbarInteraction
        } catch (e: ClassCastException) {
            Timber.tag(APP_DEBUG)
                .e("BaseAccountFragment: onAttach: $context must implement ToolbarInteraction")
        }
    }
}