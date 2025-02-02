package pt.ipt.dam.sequechat.activities

import android.content.Context
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

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getString("UserId", "") ?: ""

        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configurar botão para voltar
        binding.imageBack.setOnClickListener {
            finish()  // Fecha a ChatActivity e volta para a UsersActivity
        }

        // Receber o User da outra Activity
        val user = intent.getSerializableExtra("user") as? User
        if (user != null) {
            receiverUserId = user.username
            Toast.makeText(this, "Conversando com: ${user.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Erro ao carregar o utilizador.", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupRecyclerView()
        setupSendButton()
    }

    private fun setupRecyclerView() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                println("Fetching messages...")
                val fetchedMessages = fetchMessagesFromSheety()

                println("Messages fetched: $fetchedMessages")

                chatMessages.clear()
                chatMessages.addAll(fetchedMessages)

                chatAdapter = ChatAdapter(chatMessages, currentUserId)
                binding.chatRecyclerView.apply {
                    layoutManager = LinearLayoutManager(this@ChatActivity)
                    adapter = chatAdapter
                    chatAdapter.notifyDataSetChanged()
                    scrollToPosition(chatMessages.size - 1)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ChatActivity, "Failed to load messages.", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        messages  // Return the fetched messages
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val message = binding.inputMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                val chatMessage = ChatMessage(
                    senderId = currentUserId,
                    receiverId = receiverUserId,
                    message = message,
                    DateTime = getCurrentDateTime()
                )

                // Add the message locally and update the RecyclerView
                chatMessages.add(chatMessage)
                chatAdapter.notifyItemInserted(chatMessages.size - 1)
                binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)

                // Clear the input field
                binding.inputMessage.text.clear()

                // Store the message in Sheety
                sendMessageToSheety(chatMessage)
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

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
                    Log.d("SheetySuccess", "Message sent successfully: $responseMessage")
                } else {
                    Log.e("SheetyError", "Failed to send message: $responseCode, $responseMessage")
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChatActivity, "Failed to send message.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
