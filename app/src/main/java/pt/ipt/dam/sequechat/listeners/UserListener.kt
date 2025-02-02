package pt.ipt.dam.sequechat.listeners

import pt.ipt.dam.sequechat.models.User

// Interface que define um listener para cliques em utilizadores
interface UserListener {
    // Método chamado quando um utilizador é clicado
    fun onUserClicked(user: User)
}
