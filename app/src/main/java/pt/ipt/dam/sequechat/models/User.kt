package pt.ipt.dam.sequechat.models

import java.io.Serializable

// Classe de dados que representa um utilizador
data class User(
    val name: String,  // Nome do utilizador
    val email: String,  // Email do utilizador
    val username: String,  // Nome de utilizador
    val image: String  // Imagem de perfil codificada em Base64
) : Serializable  // Implementa Serializable para permitir a passagem de objetos entre atividades
