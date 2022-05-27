package com.rafaelboban.groupactivitytracker.ui.main.activity.adapter

import androidx.recyclerview.widget.DiffUtil
import com.rafaelboban.groupactivitytracker.data.model.EventData

class EventDiffUtil(
    private val oldItems: List<EventData>,
    private val newItems: List<EventData>,
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]

        return oldItem == newItem
    }
}