package com.rafaelboban.groupactivitytracker.ui.event.adapter

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rafaelboban.groupactivitytracker.data.model.Participant
import com.rafaelboban.groupactivitytracker.databinding.ParticipantItemBinding

class ParticipantInfoAdapter(val context: Context) : RecyclerView.Adapter<ParticipantInfoAdapter.ParticipantViewHolder>() {

    private val participants = arrayListOf<Participant>()

    class ParticipantViewHolder(val binding: ParticipantItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val binding = ParticipantItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val item = participants[position]

        holder.binding.run {
            val timeFormat = DateFormat.getTimeFormat(context)
            val updateTime = timeFormat.format(item.lastUpdateTimestamp)

            participant.text = item.name
            status.text = item.status.text
            lastUpdateTime.text = updateTime
        }
    }

    override fun getItemCount() = participants.size

    fun updateItems(newItems: List<Participant>) {
        val diff = DiffUtil.calculateDiff(ParticipantDiffUtil(participants, newItems))
        participants.clear()
        participants.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }
}