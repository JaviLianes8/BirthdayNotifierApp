package com.example.birthdaynotifier.domain.usecase

import android.content.Context
import com.example.birthdaynotifier.domain.repository.BirthdayRepository
import com.example.birthdaynotifier.domain.service.BirthdayNotifier
import java.util.*

class CheckTodaysBirthdaysUseCase(
    private val repo: BirthdayRepository,
    private val notifier: BirthdayNotifier
) {
    fun execute(context: Context) {
        val today = "%02d-%02d".format(
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
            Calendar.getInstance().get(Calendar.MONTH) + 1
        )

        repo.getAll(context).filter { it.date == today }.forEach {
            val msg = "Felicidadeeeeees!!!!, ${it.name}! 🎉🥳"
            notifier.notify(context, it.name, msg, it.phone)
        }
    }
}