package com.example.moveohealth.ui.auth

import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.moveohealth.R
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.databinding.FragmentLoginBinding
import com.example.moveohealth.model.UserType
import com.example.moveohealth.ui.auth.state.AuthStateEvent
import com.example.moveohealth.ui.auth.state.LoginFields
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.registration_fields_inputs.*
import timber.log.Timber

@AndroidEntryPoint
class LoginFragment: BaseAuthFragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
        toolbarInteraction.setToolbarTitle(getString(R.string.welcome))
        setSpannableText()
        setClickListeners()
        setInputChangeListeners()
        subscribeObservers()
    }

    private fun setSpannableText() {
        val spannable = SpannableString(getString(R.string.or_sign_up)).apply {
            setSpan(
                ForegroundColorSpan(resources.getColor(R.color.blue_app_theme)),
                3, this.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(
                StyleSpan(BOLD),
                3, this.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.textOrSignUp.text = spannable
    }

    private fun setClickListeners() {
        binding.loginButton.setOnClickListener { login() }
        binding.textOrSignUp.setOnClickListener { navRegistration() }
        binding.forgotPassword.setOnClickListener { navForgotPassword() }
    }

    private fun setInputChangeListeners() {
        binding.apply {
            editTextLoginInputEmail.doOnTextChanged { _, _, _, _ ->
                inputLayoutLoginEmail.error = null
            }
            editTextLoginInputPassword.doOnTextChanged { _, _, _, _ ->
                inputLayoutLoginPassword.error = null
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, { dataState ->
            Timber.tag(APP_DEBUG).d("LoginFragment: subscribeObservers: dataState = $dataState")
            dataState?.error?.peekContent()?.response?.message?.let {
                setErrorFieldsText(it)
            }
        })
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            Timber.tag(APP_DEBUG).d("LoginFragment: subscribeObservers: viewState = $viewState")
            viewState.loginFields?.let{ fields ->
                fields.login_email?.let{binding.editTextLoginInputEmail.setText(it)}
                fields.login_password?.let{binding.editTextLoginInputPassword.setText(it)}
                fields.login_userType.let { binding.toggleButtonUserType.check(
                    when (it) {
                        UserType.DOCTOR -> binding.buttonTypeDoctorLogin.id
                        else -> binding.buttonTypePatientLogin.id
                    })
                }
            }
        })
    }

    private fun login() {
        if (!isThereBlankField()) {
            viewModel.setStateEvent(
                AuthStateEvent.LoginAttemptEvent(
                    userType = getUserType(),
                    email = binding.editTextLoginInputEmail.text.toString(),
                    password = binding.editTextLoginInputPassword.text.toString()
                )
            )
        }
    }

    private fun getUserType(): UserType {
        val type = toggle_button_user_type.checkedButtonId.let {
            when (it) {
                binding.buttonTypeDoctorLogin.id -> UserType.DOCTOR
                else -> UserType.PATIENT
            }
        }
        viewModel.setUserType(type)
        return type
    }

    private fun isThereBlankField(): Boolean {
        var retVal = false
        binding.apply {
            if (editTextLoginInputEmail.text.isNullOrBlank()) {
                inputLayoutLoginEmail.error = " " // empty string won't show any error at all
                retVal = true
            }

            if (editTextLoginInputPassword.text.isNullOrBlank()) {
                inputLayoutLoginPassword.error = " " // empty string won't show any error at all
                retVal = true
            }
        }

        return retVal
    }

    private fun setErrorFieldsText(msg: String) {
        when (msg) {
            LoginFields.LoginError.NOT_VALID_EMAIL -> {
                binding.inputLayoutLoginEmail.error = msg
            }
        }
    }


    private fun navRegistration() {
        toolbarInteraction.setToolbarTitle(getString(R.string.sign_up)) // faster than onCreateView
        findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
    }

    private fun navForgotPassword() {
//        (activity as AuthActivity).auth_app_bar?.setExpanded(false, false)
//        (activity as AuthActivity).setAppBarTitle(getString(R.string.password)) // faster than onCreateView
//        findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setLoginFields(
            LoginFields(
                login_userType = getUserType(),
                login_email = binding.editTextLoginInputEmail.text.toString(),
                login_password = binding.editTextLoginInputPassword.text.toString()
            )
        )
        _binding = null
    }

}

