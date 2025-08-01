package com.example.birthdaynotifier.domain.model

/**
 * Domain model representing a birthday entry.
 *
 * @property name Name of the person.
 * @property date Birthday date in "dd-MM" format (e.g., "25-12").
 * @property phone Phone number associated with the birthday.
 */
data class Birthday(
    val name: String,
    val message: String,
    val date: String,
    val phone: String
)