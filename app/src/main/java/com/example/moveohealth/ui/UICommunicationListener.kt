package com.example.moveohealth.ui

import android.widget.EditText


interface UICommunicationListener {

    fun onUIMessageReceived(uiMessage: UIMessage)

    fun hideSoftKeyboard()

    fun showSoftKeyboard(editText: EditText)

    fun showProgressBar(show: Boolean)


}