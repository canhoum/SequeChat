package pt.ipt.dam.sequechat.models

import java.io.Serializable

data class User(
    val name: String,
    val email: String,

) : Serializable
