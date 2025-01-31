package pt.ipt.dam.sequechat.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pt.ipt.dam.sequechat.models.MainActivity
import pt.ipt.dam.sequechat.databinding.ActivitySignInBinding
import java.net.HttpURLConnection
import java.net.URL

class PreSignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar o ViewBinding
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()

    }

    private fun setListeners(){
        // Configurar o evento do botão "Entrar"
        binding.buttonEntrar.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Preencha todos os campos!")
            } else {
                checkLoginToSheety(email,password)
            }
        }

        // Configurar o evento do texto "Criar Nova Conta"
        binding.textCriarNovaConta.setOnClickListener {
            // Abre a Activity de registo (SignUp)
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    //Função para realizar o login
    fun checkLoginToSheety(email: String, password: String) {
        val url = "https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha1"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlObj = URL(url)
                val connection = urlObj.openConnection() as HttpURLConnection

                // Configuração da requisição GET para obter os dados
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    // Verifica se o email e password coincidem com algum utilizador registado
                    val usersArray = jsonResponse.getJSONArray("folha1")
                    var loginSuccessful = false

                    for (i in 0 until usersArray.length()) {
                        val user = usersArray.getJSONObject(i)
                        val registeredEmail = user.getString("email")
                        val registeredPassword = user.getString("password")

                        if (email == registeredEmail && password == registeredPassword) {
                            loginSuccessful = true
                            break
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (loginSuccessful) {
                            showToast("Login Bem sucedido")
                            val sharedPreferences = this@PreSignIn.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                            sharedPreferences.edit().putBoolean("IsLoggedIn", true).apply()
                            // Redireciona para a nova atividade (substituir pela tua view final)
                            startActivity(Intent(this@PreSignIn, MainActivity::class.java))
                            finish() //
                        } else {
                            showToast("Credenciais erradas!")

                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        println("Erro ao conectar ao servidor: Código $responseCode")
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    println("Erro: ${e.message}")
                }
                e.printStackTrace()
            }
        }
    }
}
