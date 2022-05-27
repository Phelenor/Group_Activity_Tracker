package com.rafaelboban.groupactivitytracker.ui.main.activity.adapter

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
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

    class EventViewHolder(val binding: EventItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = EventItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = events[position]

        holder.binding.run {
            title.text = item.participants.toString()
            val timeFormat = DateFormat.getTimeFormat(context)
            val dateFormat = DateFormat.getDateFormat(context)
            val startTime = timeFormat.format(item.startTimestamp)
            val endTime = timeFormat.format(item.endTimestamp)
            val timeRange = "$startTime - $endTime"

            title.text = item.name
            date.text = dateFormat.format(item.startTimestamp)
            participants.text = item.participants.joinToString()
            time.text = timeRange
            distance.text = if (item.distance < 1) {
                "${(item.distance * 1000).roundToInt()} m"
            } else {
                "${DecimalFormat("0.00").format(item.distance)} km"
            }
        }
    }

    override fun getItemCount() = events.size

    fun updateItems(newItems: List<EventData>) {
        val diff = DiffUtil.calculateDiff(EventDiffUtil(events, newItems))
        events.clear()
        events.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }
}