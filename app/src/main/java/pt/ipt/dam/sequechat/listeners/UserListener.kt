package pt.ipt.dam.sequechat.listeners

import pt.ipt.dam.sequechat.models.User

interface UserListener {
    fun onUserClicked(user: User)
}
