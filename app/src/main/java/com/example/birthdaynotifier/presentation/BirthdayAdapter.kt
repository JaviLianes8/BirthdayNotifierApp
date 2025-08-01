package com.example.birthdaynotifier.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.birthdaynotifier.R
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
        view.findViewById<TextView>(R.id.textDate).text = obj.getString("date")

        val msg = obj.optString("message")
        val msgView = view.findViewById<TextView>(R.id.textMessage)
        msgView.text = msg
        msgView.visibility = if (msg.isBlank()) View.GONE else View.VISIBLE

        return view
    }
}
