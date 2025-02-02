package pt.ipt.dam.sequechat.models

import java.io.Serializable

// Classe de dados que representa uma mensagem de chat
data class ChatMessage(
    val senderId: String,  // ID do remetente da mensagem
    val receiverId: String,  // ID do destinatário da mensagem
    val message: String,  // Conteúdo da mensagem
    val DateTime: String  // Data e hora da mensagem
) : Serializable  // Implementa Serializable para permitir passagem de objetos entre atividades
