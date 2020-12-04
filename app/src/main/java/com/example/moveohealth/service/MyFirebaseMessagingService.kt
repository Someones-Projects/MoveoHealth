package com.example.moveohealth.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import com.example.moveohealth.R
import com.example.moveohealth.constants.Constants
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.constants.Constants.Companion.FMC_TOKEN
import com.example.moveohealth.ui.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random


private const val CHANNEL_ID = "my_channel"

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {


    @Inject
    lateinit var dataStore: DataStore<Preferences>


//    companion object {
//        var sharedPref: SharedPreferences? = null
//
//        var token: String?
//            get() {
//                return sharedPref?.getString("token", "")
//            }
//            set(value) {
//                CoroutineScope(IO).launch {
//                    dataStore.edit {
//                        it[FMC_TOKEN] = newToken
//                    }
//                }
//            }
//    }
//
//    fun saveNewToken (newToken: String) {
//
//
//
//
//    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
//        token = newToken
        Timber.tag(APP_DEBUG).e("MyFirebaseMessagingService: onNewToken: $newToken")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.tag(APP_DEBUG).e("MyFirebaseMessagingService: onMessageReceived: message= $message")
        super.onMessageReceived(message)

        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.ic_people_gray_24)
            .setAutoCancel(true) // canceled on click
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

}