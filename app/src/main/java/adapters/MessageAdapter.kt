package adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.nearbyapp.R
import models.Message

class MessageAdapter(private val context: Context, private val items: List<Message>): BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(p0: Int): Any {
        return items[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.message_template, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val item = getItem(position) as Message

        viewHolder.sender.text = item.sender
        viewHolder.content.text = item.content

        return view
    }

    private class ViewHolder(view: View) {
        val sender: TextView = view.findViewById(R.id.name)
        val content: TextView = view.findViewById(R.id.message)
    }
}