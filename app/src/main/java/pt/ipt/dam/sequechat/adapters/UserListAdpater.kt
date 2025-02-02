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

class UserListAdpater(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit
): RecyclerView.Adapter<UserListAdpater.UserViewHolder>() {

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.textUserNameList.text = user.name

            // Carregar imagem do utilizador usando Glide
            if (user.image.isNotEmpty()) {
                try {
                    // Converter a string base64 para um array de bytes
                    val byteArray = Base64.decode(user.image, Base64.DEFAULT)

                    // Converter o array de bytes para um Bitmap
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                    // Definir a imagem diretamente no ImageView
                    binding.imageProfile.setImageBitmap(bitmap)

                } catch (e: Exception) {
                    e.printStackTrace()
                    // Se falhar, usar imagem placeholder
                    Glide.with(binding.root.context)
                        .load(R.drawable.ic_profile)  // Placeholder em caso de erro
                        .into(binding.imageProfile)
                }
            } else {
                // Caso não haja imagem, usa o placeholder
                Glide.with(binding.root.context)
                    .load(R.drawable.ic_profile)  // Placeholder
                    .into(binding.imageProfile)
            }


            binding.root.setOnClickListener {
                onUserClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

}