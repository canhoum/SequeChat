package pt.ipt.dam.sequechat.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pt.ipt.dam.sequechat.R
import pt.ipt.dam.sequechat.activities.ChatActivity
import pt.ipt.dam.sequechat.activities.PreSignIn
import pt.ipt.dam.sequechat.activities.UserActivity
import pt.ipt.dam.sequechat.activities.About
import pt.ipt.dam.sequechat.adapters.UserListAdpater
import java.net.HttpURLConnection
import java.net.URL

// Classe principal da aplicação
class MainActivity : AppCompatActivity() {

    private lateinit var usersAdapter: UserListAdpater  // Adaptador para a lista de utilizadores
    private val usersList = mutableListOf<User>()  // Lista de utilizadores a ser exibida

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Configuração da interface imersiva
        setContentView(R.layout.activity_main)

        // Inicialização da interface do utilizador
        setupUI()

        // Configuração do adaptador da RecyclerView
        usersAdapter = UserListAdpater(usersList) { selectedUser ->
            onUserSelected(selectedUser)  // Definir ação ao selecionar utilizador
        }

        findViewById<RecyclerView>(R.id.recyclerViewUsers).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = usersAdapter
        }

        // Carrega os dados de utilizador e imagem do perfil guardados nas preferências
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val image = sharedPreferences.getString("image", "")
        if (image != null && image.isNotEmpty()) {
            val imagemMain: RoundedImageView = findViewById(R.id.imageProfileMain)

            try {
                // Converte a imagem de Base64 para Bitmap
                val byteArray = android.util.Base64.decode(image, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                imagemMain.setImageBitmap(bitmap)  // Define a imagem no ImageView
            } catch (e: Exception) {
                e.printStackTrace()
                // Caso ocorra um erro, utiliza uma imagem placeholder
                imagemMain.setImageResource(R.drawable.ic_profile)
            }
        }

        val username = sharedPreferences.getString("UserId", null)
        if (username != null) {
            fetchSheetyData(username)  // Busca dados de utilizadores e mensagens associadas
        }
    }

    // Configuração dos elementos da interface
    private fun setupUI() {
        val buttonLogout: Button = findViewById(R.id.buttonLogOut)
        val fabNewChat: FloatingActionButton = findViewById(R.id.fabNewChat)
        val fabAbout: FloatingActionButton = findViewById(R.id.about)

        // Configuração do RecyclerView
        usersAdapter = UserListAdpater(usersList) { selectedUser ->
            onUserSelected(selectedUser)
        }
        findViewById<RecyclerView>(R.id.recyclerViewUsers).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = usersAdapter
        }

        // Listener para iniciar novo chat
        fabNewChat.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
        }

        // Listener para abrir a página "About"
        fabAbout.setOnClickListener {
            val intent = Intent(this, About::class.java)
            startActivity(intent)
        }

        // Listener para efetuar logout
        buttonLogout.setOnClickListener {
            logout(this)
        }

        // Ajusta a interface para o modo edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Método chamado quando um utilizador é selecionado
    private fun onUserSelected(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    // Busca dados da API Sheety para mensagens e utilizadores
    private fun fetchSheetyData(currentUserId: String) {
        val messagesUrl = "https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha2"
        val usersUrl = "https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha1"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Obter mensagens da folha2
                val messagesConnection = URL(messagesUrl).openConnection() as HttpURLConnection
                messagesConnection.requestMethod = "GET"
                messagesConnection.connect()

                val messagesResponseCode = messagesConnection.responseCode
                val receiverUsernames = mutableSetOf<String>()

                if (messagesResponseCode == HttpURLConnection.HTTP_OK) {
                    val response = messagesConnection.inputStream.bufferedReader().use { it.readText() }
                    val messagesArray = JSONObject(response).getJSONArray("folha2")

                    // Filtrar mensagens enviadas pelo utilizador atual
                    for (i in 0 until messagesArray.length()) {
                        val messageJson = messagesArray.getJSONObject(i)
                        val senderId = messageJson.getString("senderId")
                        val receiverId = messageJson.getString("receiverId")

                        if (senderId == currentUserId) {
                            receiverUsernames.add(receiverId)  // Armazenar os IDs dos destinatários
                        }
                    }
                }
                messagesConnection.disconnect()

                // Obter utilizadores da folha1
                val usersConnection = URL(usersUrl).openConnection() as HttpURLConnection
                usersConnection.requestMethod = "GET"
                usersConnection.connect()

                val usersResponseCode = usersConnection.responseCode
                if (usersResponseCode == HttpURLConnection.HTTP_OK) {
                    val response = usersConnection.inputStream.bufferedReader().use { it.readText() }
                    val usersArray = JSONObject(response).getJSONArray("folha1")

                    usersList.clear()  // Limpa a lista de utilizadores antes de adicionar novos dados
                    for (i in 0 until usersArray.length()) {
                        val userJson = usersArray.getJSONObject(i)
                        val username = userJson.getString("username")

                        // Adiciona utilizadores que são destinatários das mensagens filtradas
                        if (receiverUsernames.contains(username)) {
                            val user = User(
                                name = userJson.getString("nome"),
                                email = userJson.getString("email"),
                                username = username,
                                image = userJson.optString("image", "")
                            )
                            usersList.add(user)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        usersAdapter.notifyDataSetChanged()
                    }
                } else {
                    Log.d("MainActivity", "Erro na requisição da folha1: Código $usersResponseCode")
                }
                usersConnection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erro ao buscar utilizadores.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Efetua logout do utilizador
    private fun logout(context: Context) {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("IsLoggedIn", false).apply()
        sharedPreferences.edit().remove("UserId").apply()

        showToast("Sessão terminada!")
        val loginIntent = Intent(context, PreSignIn::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(loginIntent)
    }

    // Exibe uma mensagem de toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
