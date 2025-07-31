package com.example.birthdaynotifier

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.json.JSONArray
import java.io.File
import java.util.*

object BirthdayUtils {
    fun checkBirthdays(context: Context) {
        val file = File(context.filesDir, "birthdays.json")
        if (!file.exists()) {
            val input = context.resources.openRawResource(R.raw.birthdays)
            file.writeBytes(input.readBytes())
        }
        val birthdays = JSONArray(file.readText())
        val cal = Calendar.getInstance()
        val today = "%02d-%02d".format(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1)
        for (i in 0 until birthdays.length()) {
            val obj = birthdays.getJSONObject(i)
            val name = obj.getString("name")
            val phone = obj.getString("phone")
            val date = obj.getString("date")
            if (date == today) {
                val msg = "Felicidadeeeeees!!!!, $name! 🎉🥳"
                sendNotification(context, name, msg, phone)
            }
        }
    }

    private fun sendNotification(context: Context, name: String, message: String, phone: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val encodedMsg = Uri.encode(message)
            data = Uri.parse("https://wa.me/$phone?text=$encodedMsg")
            setPackage("com.whatsapp")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel("bday_channel", "Birthdays", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val builder = NotificationCompat.Builder(context, "bday_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Birthday: $name")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        NotificationManagerCompat.from(context).notify(name.hashCode(), builder.build())
    }
}
