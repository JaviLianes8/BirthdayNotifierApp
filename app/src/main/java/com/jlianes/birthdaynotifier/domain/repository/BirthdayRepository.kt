package com.jlianes.birthdaynotifier.domain.repository

import android.content.Context
import com.jlianes.birthdaynotifier.domain.model.Birthday

/**
 * Interface that defines access to birthday data sources.
 */
interface BirthdayRepository {

    /**
     * Loads all birthdays from the data source.
     *
     * @param context The context used to access resources or file system.
     * @return A list of [Birthday] objects.
     */
    fun getAll(context: Context): List<Birthday>
}