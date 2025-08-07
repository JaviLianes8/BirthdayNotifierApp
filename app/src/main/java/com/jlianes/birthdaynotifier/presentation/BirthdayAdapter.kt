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
 * - "date": String (e.g., "01-01")
 * - "message": Optional String
 *
 * This adapter populates a layout defined in R.layout.item_birthday.
 *
 * @param context Context used to inflate views.
 * @param items MutableList<JSONObject> List of birthday data.
 */
class BirthdayAdapter(context: Context, items: MutableList<JSONObject>) :
    ArrayAdapter<JSONObject>(context, 0, items) {

    private val todayKey = Calendar.getInstance().let {
        sortKey("%02d-%02d".format(it.get(Calendar.DAY_OF_MONTH), it.get(Calendar.MONTH) + 1))
    }
    private var nextKey: Int = -1

    fun refreshIndicators(allItems: List<JSONObject>) {
        val keys = allItems.mapNotNull { obj ->
            obj.optString("date").takeIf { it.isNotBlank() }?.let { d -> sortKey(d) }
        }
        nextKey = keys.filter { it > todayKey }.minOrNull() ?: keys.minOrNull() ?: -1
    }

    /**
     * Returns the view for a specific item in the list.
     *
     * @param position Int Index of the item in the list.
     * @param convertView View? Reusable view for optimization.
     * @param parent ViewGroup The parent view group.
     * @return View The view representing the item at the given position.
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
            key == nextKey && nextKey != -1 -> {
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

    private fun sortKey(date: String): Int {
        val parts = date.replace("/", "-").split("-")
        val day = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return month * 100 + day
    }
}
