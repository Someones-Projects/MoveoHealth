package com.example.moveohealth.api

import com.example.moveohealth.model.NotificationData

data class PushNotification(
    val data: NotificationData,
    val to: String
)