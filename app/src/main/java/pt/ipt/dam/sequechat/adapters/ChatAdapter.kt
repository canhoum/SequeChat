package pt.ipt.dam.sequechat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.sequechat.databinding.ItemContainerSentMessageBinding
import pt.ipt.dam.sequechat.databinding.ItemContainerReceivedMessageBinding
import pt.ipt.dam.sequechat.models.ChatMessage

// Adaptador de RecyclerView para exibir mensagens de chat (enviadas e recebidas)
class ChatAdapter(
    private val chatMessages: List<ChatMessage>,  // Lista de mensagens do chat
    private val currentUserId: String  // ID do utilizador atual para diferenciar mensagens enviadas e recebidas
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1  // Tipo de visualização para mensagens enviadas
        private const val VIEW_TYPE_RECEIVED = 2  // Tipo de visualização para mensagens recebidas
    }

    // Determina o tipo de visualização com base no ID do remetente da mensagem
    override fun getItemViewType(position: Int): Int {
        return if (chatMessages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    // Cria as visualizações apropriadas com base no tipo (enviada ou recebida)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemContainerSentMessageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            SentMessageViewHolder(binding)  // ViewHolder para mensagens enviadas
        } else {
            val binding = ItemContainerReceivedMessageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ReceivedMessageViewHolder(binding)  // ViewHolder para mensagens recebidas
        }
    }

    // Associa os dados da mensagem à visualização correta
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        if (holder is SentMessageViewHolder) {
            holder.setData(chatMessage)  // Define os dados para mensagens enviadas
        } else if (holder is ReceivedMessageViewHolder) {
            holder.setData(chatMessage)  // Define os dados para mensagens recebidas
        }
    }

    // Retorna o número total de mensagens na lista
    override fun getItemCount(): Int = chatMessages.size

    // ViewHolder interno para mensagens enviadas
    inner class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Associa o conteúdo da mensagem e o horário à visualização
        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message  // Define o texto da mensagem
            binding.TextDateTime.text = chatMessage.DateTime  // Define a data e hora
        }
    }

    // ViewHolder interno para mensagens recebidas
    inner class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Associa o conteúdo da mensagem e o horário à visualização
        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message  // Define o texto da mensagem
            binding.TextDateTime.text = chatMessage.DateTime  // Define a data e hora
        }
    }
}
