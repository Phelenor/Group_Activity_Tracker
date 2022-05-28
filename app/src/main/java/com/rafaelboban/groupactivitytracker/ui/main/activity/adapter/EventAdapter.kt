package com.rafaelboban.groupactivitytracker.ui.main.activity.adapter

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rafaelboban.groupactivitytracker.data.model.EventData
import com.rafaelboban.groupactivitytracker.databinding.EventItemBinding
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt

class EventAdapter(val context: Context) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private val events = arrayListOf<EventData>()

    private var listener: ((itemView: View, position: Int, item: EventData) -> Unit)? = null

    class EventViewHolder(val binding: EventItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = EventItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = events[position]

        holder.itemView.setOnClickListener {
            listener?.invoke(it, position, item)
        }

        holder.binding.run {
            title.text = item.participants.toString()
            val timeFormat = DateFormat.getTimeFormat(context)
            val dateFormat = DateFormat.getDateFormat(context)
            val date = dateFormat.format(item.startTimestamp)
            val startTime = timeFormat.format(item.startTimestamp)
            val endTime = timeFormat.format(item.endTimestamp)
            val dateTimeString = "$date $startTime - $endTime"

            title.text = item.name
            dateTime.text = dateTimeString
        }
    }

    override fun getItemCount() = events.size

    fun setOnListClickListener(listClick: (itemView: View, position: Int, item: EventData) -> Unit) {
        this.listener = listClick
    }

    fun updateItems(newItems: List<EventData>) {
        val diff = DiffUtil.calculateDiff(EventDiffUtil(events, newItems))
        events.clear()
        events.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }
}