package com.rafaelboban.groupactivitytracker.ui.event.adapter

import androidx.recyclerview.widget.DiffUtil
import com.rafaelboban.groupactivitytracker.data.model.ParticipantData

class ParticipantDiffUtil(
    private val oldItems: List<ParticipantData>,
    private val newItems: List<ParticipantData>,
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]

        return oldItem.id == newItem.id && oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]

        return oldItem == newItem
    }
}