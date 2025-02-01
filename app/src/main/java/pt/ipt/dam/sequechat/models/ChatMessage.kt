package pt.ipt.dam.sequechat.models

import java.io.Serializable

data class ChatMessage(
    val senderId: String,
    val receiverId: String,
    val message: String,
    val DateTime: String
) : Serializable
