package com.example.birthdaynotifier.framework.notification

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.birthdaynotifier.domain.service.BirthdayNotifier

/**
 * Implementation of [BirthdayNotifier] that shows a local notification
 * which opens WhatsApp with a prefilled birthday message.
 *
 * The notification will only be shown if the user granted POST_NOTIFICATIONS permission.
 * Tapping the notification opens WhatsApp chat with the contact.
 */
class WhatsAppBirthdayNotifier : BirthdayNotifier {

    /**
     * Shows a notification for a birthday with a WhatsApp message intent.
     *
     * @param context The application context used to send the notification.
     * @param name Name of the person whose birthday it is (used in title).
     * @param message Message to send via WhatsApp.
     * @param phone Recipient phone number in international format.
     */
    override fun notify(context: Context, name: String, message: String, phone: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$phone?text=${Uri.encode(message)}")
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
            val channel = NotificationChannel(
                "bday_channel",
                "Birthdays",
                NotificationManager.IMPORTANCE_DEFAULT
            )
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