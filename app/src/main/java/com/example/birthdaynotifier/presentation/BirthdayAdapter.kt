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
 */
class BirthdayAdapter(context: Context, items: MutableList<JSONObject>) :
    ArrayAdapter<JSONObject>(context, 0, items) {

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
