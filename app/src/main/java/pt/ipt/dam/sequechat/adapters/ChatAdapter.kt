package pt.ipt.dam.sequechat.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.sequechat.databinding.ItemContainerReceivedMessageBinding
import pt.ipt.dam.sequechat.databinding.ItemContainerSentMessageBinding
import pt.ipt.dam.sequechat.models.ChatMessage

class ChatAdapter(
    private val chatMessages: List<ChatMessage>,
    private val currentUserId: String,
    private val receiverProfileImage: Bitmap
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatMessages[position].senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemContainerSentMessageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemContainerReceivedMessageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        if (holder is SentMessageViewHolder) {
            holder.setData(chatMessage)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.setData(chatMessage, receiverProfileImage)
        }
    }

    override fun getItemCount(): Int = chatMessages.size

    // SentMessageViewHolder
    inner class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.TextDateTime.text = chatMessage.DateTime
        }
    }

    // ReceivedMessageViewHolder
    inner class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap) {
            binding.textMessage.text = chatMessage.message
            binding.TextDateTime.text = chatMessage.DateTime
            binding.imageProfile.setImageBitmap(receiverProfileImage)
        }
    }
}
