package com.example.moveohealth.ui


data class UIMessage(
    val message: String,
    val uiMessageType: UIMessageType
)

sealed class UIMessageType{

    object Toast : UIMessageType()

    object Dialog : UIMessageType()

    class AreYouSureDialog(
        val title: String?,
        val body: String?,
        val posBtnText: String,
        val callback: AreYouSureCallback
    ): UIMessageType()

    object None : UIMessageType()
}