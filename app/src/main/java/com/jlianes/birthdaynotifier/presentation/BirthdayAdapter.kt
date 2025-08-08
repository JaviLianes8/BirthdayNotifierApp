package com.jlianes.birthdaynotifier.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import java.util.Calendar
import com.jlianes.birthdaynotifier.R
import org.json.JSONObject

/**
 * Adapter that displays birthday entries using a custom card layout.
 *
 * Each item is a JSONObject with at least the following fields:
 * - "name": String
 * - "date": String (e.g., "01-01" or "01/01")
 * - "message": Optional String
 *
 * This adapter populates a layout defined in R.layout.item_birthday.
 *
 * Behavior:
 * - Shows a cake icon if the birthday is today.
 * - Shows a "SOON" label if the birthday occurs within the next natural month
 *   (<= same calendar day next month).
 *
 * @param context Context used to inflate views.
 * @param items MutableList<JSONObject> List of birthday data.
 */
class BirthdayAdapter(context: Context, items: MutableList<JSONObject>) :
    ArrayAdapter<JSONObject>(context, 0, items) {

    /**
     * Sort key for "today" in MMdd format, built from the current Calendar.
     * Used only to detect exact "today" matches quickly.
     */
    private val todayKey = Calendar.getInstance().let {
        sortKey("%02d-%02d".format(it.get(Calendar.DAY_OF_MONTH), it.get(Calendar.MONTH) + 1))
    }

    /**
     * Computes the number of whole days from today (00:00) until the next occurrence
     * of a given date (in format "dd-mm" or "dd/mm"). If the date this year already
     * passed, it calculates against the same day in the next year.
     *
     * @param date Birthday string in "dd-mm" or "dd/mm".
     * @return Number of days until next occurrence, or Int.MAX_VALUE on parse error.
     */
    private fun daysUntil(date: String): Int {
        val parts = date.replace("/", "-").split("-")
        val day = parts.getOrNull(0)?.toIntOrNull() ?: return Int.MAX_VALUE
        val month = parts.getOrNull(1)?.toIntOrNull() ?: return Int.MAX_VALUE

        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            if (before(now)) add(Calendar.YEAR, 1)
        }

        val diffMillis = target.timeInMillis - now.timeInMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * Checks whether a birthday falls within the next **natural month**:
     * that is, strictly after today and **on or before** the same calendar day next month.
     *
     * Examples (assuming today = Aug 8):
     * - Sep 8 → included (exactly one month).
     * - Sep 9 → excluded (beyond one natural month).
     *
     * @param date Birthday string in "dd-mm" or "dd/mm".
     * @return true if the next occurrence is within the next natural month; false otherwise.
     */
    private fun isWithinOneNaturalMonth(date: String): Boolean {
        val parts = date.replace("/", "-").split("-")
        val day = parts.getOrNull(0)?.toIntOrNull() ?: return false
        val month = parts.getOrNull(1)?.toIntOrNull() ?: return false

        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val limit = (now.clone() as Calendar).apply { add(Calendar.MONTH, 1) }

        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            if (before(now)) add(Calendar.YEAR, 1)
        }

        return target.after(now) && !target.after(limit)
    }

    /**
     * Returns the view for a specific item in the list.
     *
     * @param position Index of the item in the list.
     * @param convertView Reusable view for optimization.
     * @param parent The parent view group.
     * @return The view representing the item at the given position.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_birthday, parent, false)
        val obj = getItem(position) ?: JSONObject()

        view.findViewById<TextView>(R.id.textName).text = obj.getString("name")
        val date = obj.getString("date")
        view.findViewById<TextView>(R.id.textDate).text = date

        val cake = view.findViewById<ImageView>(R.id.imageCake)
        val soon = view.findViewById<TextView>(R.id.textSoon)
        val key = sortKey(date)

        when {
            key == todayKey -> {
                cake.visibility = View.VISIBLE
                soon.visibility = View.GONE
            }
            isWithinOneNaturalMonth(date) -> {
                cake.visibility = View.GONE
                soon.visibility = View.VISIBLE
            }
            else -> {
                cake.visibility = View.GONE
                soon.visibility = View.GONE
            }
        }

        val msg = obj.optString("message")
        val msgView = view.findViewById<TextView>(R.id.textMessage)
        msgView.text = msg
        msgView.visibility = if (msg.isBlank()) View.GONE else View.VISIBLE

        return view
    }

    /**
     * Builds a sortable integer key from a date string "dd-mm" or "dd/mm" as MMdd.
     * Used for quick equality check against today's date.
     *
     * @param date Birthday string in "dd-mm" or "dd/mm".
     * @return An integer key in MMdd format (e.g., 901 for "01-09").
     */
    private fun sortKey(date: String): Int {
        val parts = date.replace("/", "-").split("-")
        val day = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return month * 100 + day
    }
}