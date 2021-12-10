package com.app.kainta

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import kotlin.random.Random
import android.content.Intent
import android.os.Build


class MyFirebaseMessagingService : FirebaseMessagingService(){

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        val prefs = getSharedPreferences(getString(com.app.kainta.R.string.user_token), Context.MODE_PRIVATE).edit()
        prefs.putString("token", p0)
        prefs.apply()

    }


    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        val from = p0.from

        if(p0.data.isNotEmpty()){
            val titulo = p0.data["titulo"].toString()
            val detalle = p0.data["detalle"].toString()

            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                versionMayor(titulo, detalle)
            else
                versionMayor()


        }

    }

    private fun versionMayor() {
    }


    private fun versionMayor(titulo : String, detalle : String){

        val id = "mensaje"

        val nm :NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder : NotificationCompat.Builder = NotificationCompat.Builder(this, id)
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            val nc = NotificationChannel(id, "nuevo", NotificationManager.IMPORTANCE_HIGH)
            nc.setShowBadge(true)
            nm.createNotificationChannel(nc)
            }
        builder.setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(titulo)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(detalle)
            .setContentIntent(clicknoti())
            .setContentInfo("nuevo")
        val random = Random(8000)

        nm.notify(random.nextInt(), builder.build())

    }
    private fun clicknoti(): PendingIntent? {
        val nf = Intent(applicationContext, HomeActivity::class.java)
        nf.putExtra("color", "rojo")
        nf.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(this, 0, nf, 0)
    }


}

