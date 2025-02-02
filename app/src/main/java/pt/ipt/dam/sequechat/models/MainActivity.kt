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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pt.ipt.dam.sequechat.R
import pt.ipt.dam.sequechat.activities.ChatActivity
import pt.ipt.dam.sequechat.activities.PreSignIn
import pt.ipt.dam.sequechat.activities.UserActivity
import pt.ipt.dam.sequechat.adapters.UserListAdpater
import pt.ipt.dam.sequechat.models.User
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var usersAdapter: UserListAdpater
    private val usersList = mutableListOf<User>()

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inicializar UI
        setupUI()

        // Configurar o adapter e RecyclerView
        usersAdapter = UserListAdpater(usersList) { selectedUser ->
            onUserSelected(selectedUser)
        }

        findViewById<RecyclerView>(R.id.recyclerViewUsers).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = usersAdapter
        }

        // Carregar os dados da API
        fetchUsersFromSheety()
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val  username = sharedPreferences.getString("UserId", null)
        if (username != null) {
            fetchSheetyData(username)
        }
    }

    private fun setupUI() {
        val buttonLogout: Button = findViewById(R.id.buttonLogOut)
        val fabNewChat: FloatingActionButton = findViewById(R.id.fabNewChat)

        // Configurar RecyclerView e passar a função ao clicar num utilizador
        usersAdapter = UserListAdpater(usersList) { selectedUser ->
            onUserSelected(selectedUser)
        }
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewUsers).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = usersAdapter
        }

        // Listener para botão de novo chat
        fabNewChat.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
        }

        // Listener para logout
        buttonLogout.setOnClickListener {
            logout(this)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun onUserSelected(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    private fun fetchSheetyData(currentUserId: String) {
        val messagesUrl = "https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha2"
        val usersUrl = "https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha1"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Obter as mensagens da folha2
                val messagesConnection = URL(messagesUrl).openConnection() as HttpURLConnection
                messagesConnection.requestMethod = "GET"
                messagesConnection.connect()

                val messagesResponseCode = messagesConnection.responseCode
                val receiverUsernames = mutableSetOf<String>()

                if (messagesResponseCode == HttpURLConnection.HTTP_OK) {
                    val response = messagesConnection.inputStream.bufferedReader().use { it.readText() }
                    val messagesArray = JSONObject(response).getJSONArray("folha2")

                    // Filtrar mensagens em que o senderId é o currentUserId
                    for (i in 0 until messagesArray.length()) {
                        val messageJson = messagesArray.getJSONObject(i)
                        val senderId = messageJson.getString("senderId")
                        val receiverId = messageJson.getString("receiverId")

                        if (senderId == currentUserId) {
                            receiverUsernames.add(receiverId)  // Adiciona receiverId ao conjunto
                        }
                    }
                }
                messagesConnection.disconnect()

                // 2. Obter utilizadores da folha1
                val usersConnection = URL(usersUrl).openConnection() as HttpURLConnection
                usersConnection.requestMethod = "GET"
                usersConnection.connect()

                val usersResponseCode = usersConnection.responseCode
                if (usersResponseCode == HttpURLConnection.HTTP_OK) {
                    val response = usersConnection.inputStream.bufferedReader().use { it.readText() }
                    val usersArray = JSONObject(response).getJSONArray("folha1")

                    // Limpar a lista existente e adicionar novos utilizadores sem repetir
                    usersList.clear()
                    for (i in 0 until usersArray.length()) {
                        val userJson = usersArray.getJSONObject(i)
                        val username = userJson.getString("username")

                        // Adiciona apenas utilizadores cujos usernames correspondem ao receiverId filtrado
                        if (receiverUsernames.contains(username)) {
                            val user = User(
                                name = userJson.getString("nome"),
                                email = userJson.getString("email"),
                                username = username,
                                image = userJson.optString("image","")
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



    private fun fetchUsersFromSheety() {
        val url = "https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha1"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val usersArray = jsonObject.getJSONArray("folha1")

                    // Limpar a lista existente e adicionar novos utilizadores
                    usersList.clear()
                    for (i in 0 until usersArray.length()) {
                        val userJson = usersArray.getJSONObject(i)
                        val user = User(
                            name = userJson.getString("nome"),
                            email = userJson.getString("email"),
                            username = userJson.getString("username"),
                            image = userJson.optString("image","")
                        )
                        usersList.add(user)
                    }

                    withContext(Dispatchers.Main) {
                        usersAdapter.notifyDataSetChanged()
                    }
                } else {
                    Log.d("MainActivity", "Erro na requisição: Código $responseCode")
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erro ao buscar utilizadores.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun logout(context: Context) {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("IsLoggedIn", false).apply()
        sharedPreferences.edit().remove("UserId").apply()

        showToast("Sessão terminada!")
        val loginIntent = Intent(context, PreSignIn::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(loginIntent)
    }



    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
