package com.example.birthdaynotifier

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.json.JSONArray
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val buttonTest = Button(this).apply {
            text = getString(R.string.test_app)
            textSize = 24f
            setPadding(40, 40, 40, 40)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val buttonOpen = Button(this).apply {
            text = getString(R.string.open_json)
            textSize = 24f
            setPadding(40, 40, 40, 40)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        layout.addView(buttonTest)
        layout.addView(buttonOpen)

        setContentView(layout)

        buttonTest.setOnClickListener {
            checkBirthdays()
        }

        buttonOpen.setOnClickListener {
            startActivity(Intent(this, BirthdayListActivity::class.java))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        scheduleDailyCheck()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkBirthdays() {
        val file = File(filesDir, "birthdays.json")
        if (!file.exists()) {
            val input = resources.openRawResource(R.raw.birthdays)
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
                sendNotification(name, msg, phone)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(name: String, message: String, phone: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val encodedMsg = Uri.encode(message)
            data = Uri.parse("https://wa.me/$phone?text=$encodedMsg")
            setPackage("com.whatsapp")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "bday_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Birthday: $name")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "bday_channel",
                "Birthdays",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(this).notify(name.hashCode(), builder.build())
    }

    private fun scheduleDailyCheck() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BirthdayReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}