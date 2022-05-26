package com.rafaelboban.groupactivitytracker.ui.event.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.data.socket.Announcement
import com.rafaelboban.groupactivitytracker.data.socket.BaseModel
import com.rafaelboban.groupactivitytracker.data.socket.ChatMessage
import com.rafaelboban.groupactivitytracker.databinding.AnnouncementItemBinding
import com.rafaelboban.groupactivitytracker.databinding.ChatItemLeftBinding
import com.rafaelboban.groupactivitytracker.databinding.ChatItemRightBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val VIEW_TYPE_LEFT_MESSAGE = 0
private const val VIEW_TYPE_RIGHT_MESSAGE = 1
private const val VIEW_TYPE_ANNOUNCEMENT = 2

class ChatAdapter(val context: Context, private val userId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var chatItems = listOf<BaseModel>()

    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    class MessageLeftViewHolder(val binding: ChatItemLeftBinding) : RecyclerView.ViewHolder(binding.root)

    class MessageRightViewHolder(val binding: ChatItemRightBinding) : RecyclerView.ViewHolder(binding.root)

    class AnnouncementViewHolder(val binding: AnnouncementItemBinding) : RecyclerView.ViewHolder(binding.root)

    suspend fun updateDataset(newDataset: List<BaseModel>) = withContext(Dispatchers.Default) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun getOldListSize() = chatItems.size

            override fun getNewListSize() = newDataset.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = chatItems[oldItemPosition]
                val newItem = newDataset[newItemPosition]

                return if (oldItem is ChatMessage && newItem is ChatMessage) {
                    oldItem.eventId == newItem.eventId && oldItem.fromId == newItem.fromId && oldItem.timestamp == newItem.timestamp
                } else {
                    oldItem == newItem
                }
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return chatItems[oldItemPosition] == newDataset[newItemPosition]
            }
        })

        withContext(Dispatchers.Main) {
            chatItems = newDataset
            diffResult.dispatchUpdatesTo(this@ChatAdapter)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val chatObject = chatItems[position]) {
            is Announcement -> VIEW_TYPE_ANNOUNCEMENT
            is ChatMessage -> if (userId == chatObject.fromId) VIEW_TYPE_RIGHT_MESSAGE else VIEW_TYPE_LEFT_MESSAGE
            else -> throw IllegalStateException()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LEFT_MESSAGE -> MessageLeftViewHolder(
                ChatItemLeftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            VIEW_TYPE_RIGHT_MESSAGE -> MessageRightViewHolder(
                ChatItemRightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            VIEW_TYPE_ANNOUNCEMENT -> AnnouncementViewHolder(
                AnnouncementItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AnnouncementViewHolder -> {
                val item = chatItems[position] as Announcement
                holder.binding.run {
                    announcement.text = item.message
                    time.text = dateFormat.format(item.timestamp)

                    when (item.announcementType) {
                        Announcement.TYPE_PLAYER_JOINED -> {
                            root.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.light_green))
                        }
                        Announcement.TYPE_PLAYER_LEFT -> {
                            root.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.light_yellow))
                        }
                        Announcement.TYPE_PLAYER_HELP -> {
                            root.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.help_red))
                        }
                        Announcement.TYPE_PLAYER_HELP_CLEAR -> {
                            root.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.light_orange))
                        }
                    }
                }
            }
            is MessageLeftViewHolder -> {
                val item = chatItems[position] as ChatMessage
                holder.binding.run {
                    message.text = item.message
                    username.text = item.fromUsername
                    time.text = dateFormat.format(item.timestamp)
                }
            }
            is MessageRightViewHolder -> {
                val item = chatItems[position] as ChatMessage
                holder.binding.run {
                    message.text = item.message
                    username.text = item.fromUsername
                    time.text = dateFormat.format(item.timestamp)
                }
            }
        }
    }

    override fun getItemCount() = chatItems.size
}