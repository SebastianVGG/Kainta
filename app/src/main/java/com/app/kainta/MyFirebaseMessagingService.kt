package com.app.kainta

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.app.NotificationManagerCompat

import android.R
import android.app.Notification
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.HashMap


class MyFirebaseMessagingService : FirebaseMessagingService(){

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        val tokenData = HashMap<String, Any>()
        tokenData["token"] = p0
        val firebase = FirebaseFirestore.getInstance()
        firebase.collection("DeviceTokens").document()
            .set(tokenData)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)


        val title: String? = remoteMessage.notification?.title
        val text: String? = remoteMessage.notification?.body
        val CHANNEL_ID = "HEADS_UP_NOTIFICATION"

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Heads Up Notification",
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification: Notification.Builder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.button_onoff_indicator_off)
            .setAutoCancel(true)
        NotificationManagerCompat.from(this).notify(1, notification.build())



    }

}

