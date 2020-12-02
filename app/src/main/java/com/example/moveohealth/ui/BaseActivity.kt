package com.example.moveohealth.ui

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.moveohealth.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


abstract class BaseActivity: AppCompatActivity(),
    DataStateChangeListener,
    UICommunicationListener
{

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onUIMessageReceived(uiMessage: UIMessage) {
        when(uiMessage.uiMessageType) {

            is UIMessageType.AreYouSureDialog -> {
                uiMessage.uiMessageType.let {
                    areYouSureDialog(
                        title = it.title,
                        body = it.body,
                        posBtnText = it.posBtnText,
                        callback = it.callback
                    )
                }
            }

            is UIMessageType.Toast -> {
                displayToast(uiMessage.message)
            }

            is UIMessageType.Dialog -> {
                displayInfoDialog(uiMessage.message)
            }

            is UIMessageType.None -> {
            }
        }
    }

    override fun onDataStateChange(dataState: DataState<*>?) {
        if (dataState != null) {
            CoroutineScope(Dispatchers.Main).launch{
                showProgressBar(dataState.loading.isLoading)

                dataState.error?.let { errorEvent ->
                    handleStateError(errorEvent)
                }

                dataState.success?.let {
                    it.response?.let { responseEvent ->
                        handleStateSuccessResponse(responseEvent)
                    }
                }
            }
        }
    }

    private fun handleStateError(errorEvent: Event<StateError>) {
        errorEvent.getContentIfNotHandled()?.let{
            when(it.response.responseType){
                is ResponseType.Toast ->{
                    it.response.message?.let{message ->
                        displayToast(message)
                    }
                }
                is ResponseType.Dialog ->{
                    it.response.message?.let{ message ->
                        displayErrorDialog(message)
                    }
                }
                is ResponseType.None -> {
                }
            }
        }
    }

    private fun handleStateSuccessResponse(event: Event<Response>){
        event.getContentIfNotHandled()?.let{
            when(it.responseType){
                is ResponseType.Toast ->{
                    it.message?.let{ message ->
                        displayToast(message)
                    }
                }
                is ResponseType.Dialog ->{
                    it.message?.let{ message ->
                        displaySuccessDialog(message)
                    }
                }
                is ResponseType.None -> {
                }
            }
        }
    }

    override fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    override fun showSoftKeyboard(editText: EditText) {
        editText.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
}