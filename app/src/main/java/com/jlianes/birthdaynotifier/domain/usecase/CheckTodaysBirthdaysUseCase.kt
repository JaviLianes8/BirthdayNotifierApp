package com.jlianes.birthdaynotifier.domain.usecase

import android.content.Context
import android.util.Log
import com.jlianes.birthdaynotifier.R
import com.jlianes.birthdaynotifier.domain.repository.BirthdayRepository
import com.jlianes.birthdaynotifier.domain.service.BirthdayNotifier
import java.util.*

/**
 * Use case that checks if today is the birthday of any registered person
 * and sends a notification through the given [BirthdayNotifier].
 *
 * @property repo Repository to retrieve birthday data.
 * @property notifier Notifier implementation used to send birthday messages.
 */
class CheckTodaysBirthdaysUseCase(
    private val repo: BirthdayRepository,
    private val notifier: BirthdayNotifier
) {
    /**
     * Executes the use case by checking today's date and notifying
     * each birthday match using the provided [notifier].
     *
     * @param context Android context used for data access and notification.
     */
    fun execute(context: Context) {
        val today = "%02d-%02d".format(
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
            Calendar.getInstance().get(Calendar.MONTH) + 1
        )

        repo.getAll(context)
            .onEach { Log.d("BirthdayTest", "Entry: ${it.name} -> '${it.date}'") }
            .filter { it.date.replace("/", "-").trim() == today }
            .forEach {
                Log.d("BirthdayTest", "Sending to ${it.name}")
                val msg = it.message.ifBlank { context.getString(R.string.default_message, it.name) }
                notifier.notify(context, it.name, msg, it.phone)
            }

        Log.d("BirthdayTest", "Today is $today")

        val list = repo.getAll(context)
        Log.d("BirthdayTest", "Found ${list.size} birthdays")

        list.filter { it.date == today }.forEach {
            Log.d("BirthdayTest", "Sending to ${it.name}")
            val msg = it.message.ifBlank { context.getString(R.string.default_message, it.name) }
            notifier.notify(context, it.name, msg, it.phone)
        }
    }

}