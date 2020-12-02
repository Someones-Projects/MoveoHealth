package com.example.moveohealth.ui.auth

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.TranslateAnimation
import androidx.activity.addCallback
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.moveohealth.R
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.model.UserType
import com.example.moveohealth.ui.auth.state.AuthStateEvent
import com.example.moveohealth.ui.auth.state.RegistrationFields
import com.example.moveohealth.ui.auth.state.RegistrationFields.RegistrationErrors.Companion.NOT_VALID_EMAIL
import com.example.moveohealth.ui.auth.state.RegistrationFields.RegistrationErrors.Companion.PASSWORD_CONTAINS_6_CHARS
import com.example.moveohealth.ui.auth.state.RegistrationFields.RegistrationErrors.Companion.PASSWORD_DONT_MATCH
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.layout_verification_email_sent_message.*
import kotlinx.android.synthetic.main.registration_fields_inputs.*
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class RegisterFragment: BaseAuthFragment(R.layout.fragment_register) {

    // cant add bindings here b/c fragment has <include> layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cancelActiveJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarInteraction.setToolbarTitle(getString(R.string.sign_up))
        setClickListeners()
        setTextEditListeners()
        subscribeObservers()
        requireActivity().onBackPressedDispatcher.addCallback(this) { popStackBack() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                popStackBack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, { dataState ->
            Timber.tag(APP_DEBUG).d("RegisterFragment: subscribeObservers: dataState = $dataState")

            // TODO: handle email confirmation
//            dataState?.success?.response?.peekContent()?.message?.let {
//                if (it == SUCCESS_CONFIRM_EMAIL_ADDRESS_SENT) {
//                    onConfirmEmailLinkSent()
//                }
//            }
            dataState?.error?.peekContent()?.response?.message?.let {
                setErrorFieldsText(it)
            }
        })
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            Timber.tag(APP_DEBUG).d("RegisterFragment: subscribeObservers: viewState = $viewState   ")
            viewState.registrationFields?.let { fields ->
                fields.registration_username?.let { edit_text_registration_input_username.setText(it) }
                fields.registration_email?.let{edit_text_registration_input_email.setText(it)}
                fields.registration_password?.let{edit_text_registration_input_password.setText(it)}
                fields.registration_confirm_password?.let{edit_text_registration_input_password_confirm.setText(it)}
                fields.registration_userType.let {
                    toggle_button_user_type_sign_up.check(
                        when (it) {
                            UserType.DOCTOR -> R.id.button_type_doctor
                            else -> R.id.button_type_patient
                        }
                    )
                }
            }
        })
    }

    private fun setErrorFieldsText(msg: String) {
        when (msg) {
            NOT_VALID_EMAIL -> {
                input_layout_registration_email.error = msg
            }

            PASSWORD_DONT_MATCH -> {
                input_layout_registration_password_confirm.error = msg
            }

            PASSWORD_CONTAINS_6_CHARS -> {
                input_layout_registration_password.error = msg
            }
        }
    }

    private fun setClickListeners() {
        register_button.setOnClickListener {
            register()
        }

        return_to_launcher_fragment.setOnClickListener {
            findNavController().popBackStack()
        }



    }

    private fun setTextEditListeners() {
        edit_text_registration_input_email.doOnTextChanged { _, _, _, _ ->
            input_layout_registration_email.error = null
        }

        edit_text_registration_input_password.doOnTextChanged { _, _, _, _ ->
            input_layout_registration_password.error = null
        }

        edit_text_registration_input_password_confirm.doOnTextChanged { _, _, _, _ ->
            input_layout_registration_password_confirm.error = null
        }
    }

    private fun resetAllErrorFields() {
        input_layout_registration_email.error = null
        input_layout_registration_password.error = null
        input_layout_registration_password_confirm.error = null
    }

    private fun register() {
        if (!isThereBlankField()) {
            resetAllErrorFields()
            viewModel.setStateEvent(
                AuthStateEvent.RegisterAttemptEvent(
                    username = edit_text_registration_input_username.text.toString(),
                    userType = getUserType(),
                    email = edit_text_registration_input_email.text.toString().toLowerCase(Locale.ROOT),
                    password = edit_text_registration_input_password.text.toString(),
                    confirm_password = edit_text_registration_input_password_confirm.text.toString()
                )
            )
        }
        uiCommunicationListener.hideSoftKeyboard()
    }

    private fun getUserType(): UserType {
        val type = toggle_button_user_type_sign_up.checkedButtonId.let {
            when (it) {
                R.id.button_type_doctor -> UserType.DOCTOR
                else -> UserType.PATIENT
            }
        }
        viewModel.setUserType(type)
        return type
    }

    private fun isThereBlankField(): Boolean {
        var retVal = false
        if (edit_text_registration_input_email.text.isNullOrBlank()) {
            input_layout_registration_email.error = " " // empty string won't show any error at all
            retVal = true
        }

        if (edit_text_registration_input_password.text.isNullOrBlank()) {
            input_layout_registration_password.error = " " // empty string won't show any error at all
            retVal = true
        }

        if (edit_text_registration_input_password_confirm.text.isNullOrBlank()) {
            input_layout_registration_password_confirm.error = " " // empty string won't show any error at all
            retVal = true
        }
        return retVal
    }

    private fun onConfirmEmailLinkSent() {
        saveRegistrationFieldsViewState()
        registration_fields_container.visibility = View.INVISIBLE
        confirm_email_sent_title?.text = getString(R.string.confirm_email_address_sent, viewModel.getRegistrationEmail())
        val animation = TranslateAnimation(
            confirm_email_sent_container.width.toFloat(),
            0f,
            0f,
            0f
        )
        animation.duration = 500
        confirm_email_sent_container.startAnimation(animation)
        confirm_email_sent_container.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveRegistrationFieldsViewState()
    }

    private fun saveRegistrationFieldsViewState() {
        viewModel.setRegistrationFields(
            RegistrationFields(
                registration_username = edit_text_registration_input_username.text.toString(),
                registration_userType = getUserType(),
                registration_email = edit_text_registration_input_email.text.toString(),
                registration_password = edit_text_registration_input_password.text.toString(),
                registration_confirm_password = edit_text_registration_input_password_confirm.text.toString()
            )
        )
    }

    private fun popStackBack() {
        findNavController().popBackStack()
        toolbarInteraction.setToolbarTitle(getString(R.string.welcome))
    }

}
