package pt.ipt.dam.sequechat.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.*
import android.content.Intent
import org.json.JSONObject
import pt.ipt.dam.sequechat.adapters.UsersAdapter
import pt.ipt.dam.sequechat.databinding.ActivityUsersBinding
import pt.ipt.dam.sequechat.listeners.UserListener
import pt.ipt.dam.sequechat.models.User
import pt.ipt.dam.sequechat.utilities.PreferenceManager
import java.net.HttpURLConnection
import java.net.URL

class UserActivity : AppCompatActivity(), UserListener {

    private lateinit var binding: ActivityUsersBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var usersAdapter: UsersAdapter
    private val usersList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)
        setupUI()
        fetchSheetyData()
    }

    private fun setupUI() {
        binding.imageBack.setOnClickListener { onBackPressed() }
        binding.usersRecyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    override fun onUserClicked(user: User) {
        Toast.makeText(this, "User clicked: ${user.name}", Toast.LENGTH_SHORT).show()
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
        finish()
    }

    private fun fetchSheetyData() {
        showLoading(true)

        val url = "https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha1"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("API_RESPONSE", "Resposta da API: $response")

                    val jsonObject = JSONObject(response)
                    val folha1 = jsonObject.getJSONArray("folha1")

                    for (i in 0 until folha1.length()) {
                        val userJson = folha1.getJSONObject(i)
                        val user = User(
                            name = userJson.getString("nome"),
                            email = userJson.getString("email"),
                            username = userJson.optString("username", "N/A")  // Verifica se o campo 'user' está presente
                        )
                        usersList.add(user)
                    }

                    withContext(Dispatchers.Main) {
                        if (usersList.isNotEmpty()) {
                            usersAdapter = UsersAdapter(usersList, this@UserActivity)
                            binding.usersRecyclerView.adapter = usersAdapter
                            binding.usersRecyclerView.visibility = View.VISIBLE
                        } else {
                            showErrorMessage()
                        }
                        showLoading(false)
                    }
                } else {
                    Log.d("UserActivity", "Erro na requisição: Código $responseCode")
                    showLoading(false)
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToast("Erro ao obter dados.")
                    showLoading(false)
                }
            }
        }
    }

    private fun updateUserField(id: Int, username: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updateJsonBody = """
                    {
                        "folha1": {
                            "User": "$username"
                        }
                    }
                """.trimIndent()

                Log.d("PUT_JSON_BODY", updateJsonBody)

                val url = URL("https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha1/$id")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    outputStream.write(updateJsonBody.toByteArray())
                }

                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

                Log.e("PUT_RESPONSE", "Código: $responseCode, Resposta: $responseMessage")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UserActivity, "Campo User atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("SheetyError", "Erro ao atualizar Username: Código $responseCode, Resposta: $responseMessage")
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserActivity, "Erro ao atualizar User.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showErrorMessage() {
        binding.textinputError.apply {
            text = "No user available"
            visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
