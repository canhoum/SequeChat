package pt.ipt.dam.sequechat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.sequechat.databinding.ItemCountainerUserBinding
import pt.ipt.dam.sequechat.listeners.UserListener
import pt.ipt.dam.sequechat.models.User

// Adaptador de RecyclerView para exibir a lista de utilizadores
class UsersAdapter(
    private val users: List<User>,  // Lista de utilizadores
    private val userListener: UserListener  // Listener para manipular cliques nos utilizadores
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    // Cria e retorna o ViewHolder para um item da lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemContainerUserBinding = ItemCountainerUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(itemContainerUserBinding)
    }

    // Associa os dados do utilizador ao ViewHolder na posição especificada
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(users[position])
    }

    // Retorna o número total de utilizadores na lista
    override fun getItemCount(): Int = users.size

    // ViewHolder interno que contém a lógica para exibir os dados do utilizador
    inner class UserViewHolder(private val binding: ItemCountainerUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Define os dados do utilizador nos componentes visuais
        fun setUserData(user: User) {
            binding.textName.text = user.name  // Define o nome do utilizador
            binding.textEmail.text = user.email  // Define o email do utilizador

            // Define o evento de clique no item da lista
            binding.root.setOnClickListener {
                userListener.onUserClicked(user)  // Chama o método do listener quando o utilizador é clicado
            }
        }
    }

    // Converte uma imagem codificada em Base64 para um objeto Bitmap
    private fun getUserImage(encodedImage: String): Bitmap {
        val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
