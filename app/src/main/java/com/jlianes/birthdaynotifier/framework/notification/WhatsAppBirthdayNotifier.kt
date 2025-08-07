package com.jlianes.birthdaynotifier.framework.notification

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.jlianes.birthdaynotifier.domain.service.BirthdayNotifier
import com.jlianes.birthdaynotifier.R
import com.jlianes.birthdaynotifier.framework.receiver.SnoozeReceiver

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
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra(SnoozeReceiver.EXTRA_NAME, name)
            putExtra(SnoozeReceiver.EXTRA_MESSAGE, message)
            putExtra(SnoozeReceiver.EXTRA_PHONE, phone)
        }

        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            name.hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remoteInput = RemoteInput.Builder(SnoozeReceiver.KEY_SNOOZE_HOURS)
            .setLabel(context.getString(R.string.snooze_label))
            .setChoices(arrayOf("1", "2", "3", "4"))
            .build()

        val action = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_recent_history,
            context.getString(R.string.snooze_action),
            snoozePendingIntent
        ).addRemoteInput(remoteInput).build()

        val builder = NotificationCompat.Builder(context, "bday_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_title, name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(action)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(name.hashCode(), builder.build())
    }
}