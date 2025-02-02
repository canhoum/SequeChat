package pt.ipt.dam.sequechat.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipt.dam.sequechat.R
import pt.ipt.dam.sequechat.databinding.ItemUserBinding
import pt.ipt.dam.sequechat.models.User

// Adaptador de RecyclerView para listar os utilizadores
class UserListAdpater(
    private val users: List<User>,  // Lista de utilizadores
    private val onUserClick: (User) -> Unit  // Função de callback chamada quando um utilizador é clicado
) : RecyclerView.Adapter<UserListAdpater.UserViewHolder>() {

    // ViewHolder interno que contém a lógica para exibir os dados do utilizador
    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Associa os dados do utilizador à interface
        fun bind(user: User) {
            binding.textUserNameList.text = user.name  // Define o nome do utilizador na interface

            // Carregar a imagem do perfil do utilizador
            if (user.image.isNotEmpty()) {
                try {
                    // Converter a string Base64 para um array de bytes
                    val byteArray = Base64.decode(user.image, Base64.DEFAULT)

                    // Converter o array de bytes para um Bitmap
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                    // Definir o bitmap diretamente no ImageView
                    binding.imageProfile.setImageBitmap(bitmap)

                } catch (e: Exception) {
                    e.printStackTrace()
                    // Se ocorrer um erro, utilizar a imagem placeholder
                    Glide.with(binding.root.context)
                        .load(R.drawable.ic_profile)  // Imagem de substituição
                        .into(binding.imageProfile)
                }
            } else {
                // Se não houver imagem disponível, utilizar o placeholder
                Glide.with(binding.root.context)
                    .load(R.drawable.ic_profile)
                    .into(binding.imageProfile)
            }

            // Define o evento de clique no item da lista
            binding.root.setOnClickListener {
                onUserClick(user)  // Chama a função de callback com o utilizador selecionado
            }
        }
    }

    // Cria um novo ViewHolder para cada item na lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    // Associa os dados do utilizador ao ViewHolder na posição especificada
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    // Retorna o número total de utilizadores na lista
    override fun getItemCount(): Int = users.size
}
