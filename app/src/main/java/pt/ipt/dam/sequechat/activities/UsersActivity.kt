package pt.ipt.dam.sequechat.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.*
import org.json.JSONObject
import pt.ipt.dam.sequechat.adapters.UsersAdapter
import pt.ipt.dam.sequechat.databinding.ActivityUsersBinding
import pt.ipt.dam.sequechat.models.User
import pt.ipt.dam.sequechat.utilities.PreferenceManager
import java.net.HttpURLConnection
import java.net.URL

class UserActivity : AppCompatActivity() {

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

    private fun showErrorMessage() {
        binding.textinputError.apply {
            text = "No user available"
            visibility = View.VISIBLE
        }
    }

    private fun fetchSheetyData() {
        showLoading(true)  // Mostrar o ProgressBar antes de iniciar o carregamento

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
                            email = userJson.getString("email")
                        )
                        Log.d("UserActivity", "Utilizador recebido: ${user.name}, ${user.email}")
                        usersList.add(user)
                    }

                    withContext(Dispatchers.Main) {
                        if (usersList.isNotEmpty()) {
                            usersAdapter = UsersAdapter(usersList)
                            binding.usersRecyclerView.adapter = usersAdapter
                            binding.usersRecyclerView.visibility = View.VISIBLE
                        } else {
                            showErrorMessage()
                        }
                        showLoading(false)  // Esconder o ProgressBar após carregar os dados
                    }
                } else {
                    Log.d("UserActivity", "Erro na requisição: Código $responseCode")
                    showLoading(false)  // Esconder o ProgressBar mesmo em caso de erro
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToast("Erro ao obter dados.")
                    showLoading(false)  // Esconder o ProgressBar após erro
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
