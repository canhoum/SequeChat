package pt.ipt.dam.sequechat.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import org.json.JSONObject
import pt.ipt.dam.sequechat.adapters.ChatAdapter
import pt.ipt.dam.sequechat.databinding.ActivityChatBinding
import pt.ipt.dam.sequechat.models.ChatMessage
import pt.ipt.dam.sequechat.models.MainActivity
import pt.ipt.dam.sequechat.models.User
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var currentUserId: String
    private lateinit var receiverUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtém o ID do utilizador atual das SharedPreferences
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getString("UserId", "") ?: ""

        // Verifica se há utilizador logado
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Nenhum utilizador logado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configura o botão para voltar à MainActivity
        binding.imageBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Obtém o utilizador destinatário da outra activity
        val user = intent.getSerializableExtra("user") as? User
        if (user != null) {
            receiverUserId = user.username  // ID do utilizador destinatário
            Toast.makeText(this, "Conversando com: ${user.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Erro ao carregar o utilizador.", Toast.LENGTH_SHORT).show()
            finish()  // Se o utilizador não for encontrado, fecha a activity
        }

        setupRecyclerView()
        setupSendButton()
    }

    // Configura o RecyclerView para exibir as mensagens
    private fun setupRecyclerView() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                println("A obter mensagens...")
                val fetchedMessages = fetchMessagesFromSheety()  // Obtém as mensagens da API

                println("Mensagens obtidas: $fetchedMessages")

                // Limpa a lista atual e adiciona as novas mensagens
                chatMessages.clear()
                chatMessages.addAll(fetchedMessages)

                // Configura o adaptador para o RecyclerView
                chatAdapter = ChatAdapter(chatMessages, currentUserId)
                binding.chatRecyclerView.apply {
                    layoutManager = LinearLayoutManager(this@ChatActivity)
                    adapter = chatAdapter
                    chatAdapter.notifyDataSetChanged()
                    scrollToPosition(chatMessages.size - 1)  // Move para a última mensagem
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ChatActivity, "Falha ao carregar mensagens.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Obtém as mensagens da API Sheety usando coroutines
    private suspend fun fetchMessagesFromSheety(): List<ChatMessage> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<ChatMessage>()
        try {
            val url = URL("https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha2")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONObject(response).getJSONArray("folha2")

                // Filtra as mensagens entre o utilizador atual e o destinatário
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    if ((jsonObject.getString("senderId") == currentUserId && jsonObject.getString("receiverId") == receiverUserId) ||
                        (jsonObject.getString("senderId") == receiverUserId && jsonObject.getString("receiverId") == currentUserId)
                    ) {
                        val chatMessage = ChatMessage(
                            senderId = jsonObject.getString("senderId"),
                            receiverId = jsonObject.getString("receiverId"),
                            message = jsonObject.getString("message"),
                            DateTime = jsonObject.getString("dateTime")
                        )
                        messages.add(chatMessage)
                    }
                }
            }
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        messages  // Retorna as mensagens obtidas
    }

    // Configura o botão para enviar mensagens
    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val message = binding.inputMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                val chatMessage = ChatMessage(
                    senderId = currentUserId,
                    receiverId = receiverUserId,
                    message = message,
                    DateTime = getCurrentDateTime()  // Obtém a data e hora atuais
                )

                // Adiciona a mensagem localmente e atualiza o RecyclerView
                chatMessages.add(chatMessage)
                chatAdapter.notifyItemInserted(chatMessages.size - 1)
                binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)

                // Limpa o campo de input
                binding.inputMessage.text.clear()

                // Envia a mensagem para a API Sheety
                sendMessageToSheety(chatMessage)
            } else {
                Toast.makeText(this, "Por favor, insira uma mensagem.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Obtém a data e hora atuais formatadas
    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Envia a mensagem para a API Sheety
    private fun sendMessageToSheety(chatMessage: ChatMessage) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonBody = """
                    {
                        "folha2": {
                            "senderId": "${chatMessage.senderId}",
                            "receiverId": "${chatMessage.receiverId}",
                            "message": "${chatMessage.message}",
                            "dateTime": "${chatMessage.DateTime}"
                        }
                    }
                """.trimIndent()

                val url = URL("https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha2")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    outputStream.write(jsonBody.toByteArray())
                }

                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    Log.d("SheetySuccess", "Mensagem enviada com sucesso: $responseMessage")
                } else {
                    Log.e("SheetyError", "Falha ao enviar mensagem: $responseCode, $responseMessage")
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChatActivity, "Falha ao enviar mensagem.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
