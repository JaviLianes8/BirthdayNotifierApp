package com.jlianes.birthdaynotifier.domain.service

import android.content.Context

/**
 * Abstraction for sending birthday notifications to users.
 */
interface BirthdayNotifier {

    /**
     * Sends a birthday message to the specified recipient.
     *
     * @param context The context used for sending the notification.
     * @param name The name of the birthday person.
     * @param message The message to be sent.
     * @param phone The phone number of the recipient.
     */
    fun notify(context: Context, name: String, message: String, phone: String)
}