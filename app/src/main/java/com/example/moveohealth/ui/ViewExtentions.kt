package com.example.moveohealth.ui

import android.app.Activity
import android.content.DialogInterface
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.forEach
import com.example.moveohealth.R
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder


fun Activity.displayToast(@StringRes message:Int){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Activity.displayToast(message:String){
    Toast.makeText(this,message, Toast.LENGTH_LONG).show()
}

fun Activity.displaySuccessDialog(message: String?){
    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.text_success)
        .setMessage(message)
        .setPositiveButton(R.string.text_ok) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun Activity.displayErrorDialog(errorMessage: String?) {
    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.text_error)
        .setMessage(errorMessage)
        .setPositiveButton(R.string.text_ok) { dialog, _ ->
            dialog.dismiss()
        }
        .show()

}

fun Activity.displayInfoDialog(message: String?){
    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.text_info)
        .setMessage(message)
        .setPositiveButton(R.string.text_ok) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun Activity.areYouSureDialog(title: String?, body: String?, posBtnText: String, callback: AreYouSureCallback) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(body)
        .setPositiveButton(posBtnText) { dialog, _ ->
            dialog.cancel()
            callback.proceed()
        }
        .setNegativeButton(R.string.text_cancel) { dialog, _ ->
            dialog.cancel()
        }
        .show()
}

fun Activity.displayTakeActionDialog(
    title: String?,
    body: String?,
    posBtnText: String,
    positiveClickListener: DialogInterface.OnClickListener
): AlertDialog {
    return MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(body)
        .setPositiveButton(posBtnText, positiveClickListener)
        .setNegativeButton(R.string.text_cancel) { dialog, _ ->
            dialog.cancel()
        }
        .show()
}


interface AreYouSureCallback {

    fun proceed()

    fun cancel()
}

