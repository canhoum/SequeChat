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

    private lateinit var binding: ActivitySignInBinding  // Instância do ViewBinding para aceder aos elementos da interface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciar o ViewBinding e associar à view atual
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()  // Configurar os listeners para os botões
    }

    // Função que define os eventos (listeners) dos botões
    private fun setListeners() {
        // Configurar o evento do botão "Entrar"
        binding.buttonEntrar.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()  // Obter o texto do campo de email
            val password = binding.inputPassword.text.toString().trim()  // Obter o texto do campo de password

            // Verificar se os campos não estão vazios
            if (email.isEmpty() || password.isEmpty()) {
                showToast("Preencha todos os campos!")
            } else {
                checkLoginToSheety(email, password)  // Verificar o login com a API Sheety
            }
        }

        // Configurar o evento do texto "Criar Nova Conta"
        binding.textCriarNovaConta.setOnClickListener {
            // Abre a Activity de registo (SignUpActivity)
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    // Função para exibir uma mensagem toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Função para verificar o login usando a API Sheety
    private fun checkLoginToSheety(email: String, password: String) {
        val url = "https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha1"  // URL da API Sheety

        // Executar a requisição num contexto de IO para evitar bloqueio da UI
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlObj = URL(url)
                val connection = urlObj.openConnection() as HttpURLConnection

                // Configuração da requisição GET
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Obter a resposta da API e convertê-la para JSON
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    // Verificar as credenciais de todos os utilizadores registados
                    val usersArray = jsonResponse.getJSONArray("folha1")
                    var loginSuccessful = false

                    for (i in 0 until usersArray.length()) {
                        val user = usersArray.getJSONObject(i)
                        val registeredEmail = user.getString("email")  // Email registado
                        val registeredPassword = user.getString("password")  // Password registada
                        val registeredUsername = user.getString("username")  // Nome de utilizador registado
                        val registeredImage = user.optString("image", "")  // Imagem associada ao utilizador

                        // Verifica se o email e a password inseridos coincidem com algum utilizador registado
                        if (email == registeredEmail && password == registeredPassword) {
                            val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                            sharedPreferences.edit().apply {
                                putString("UserId", registeredUsername)  // Guardar o nome de utilizador
                                putString("image", registeredImage)  // Guardar a imagem associada
                                apply()
                            }
                            loginSuccessful = true  // Indica que o login foi bem-sucedido
                            break
                        }
                    }

                    // Atualiza a UI no contexto principal (Main Thread)
                    withContext(Dispatchers.Main) {
                        if (loginSuccessful) {
                            showToast("Login bem-sucedido")
                            val sharedPreferences = this@PreSignIn.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                            sharedPreferences.edit().putBoolean("IsLoggedIn", true).apply()

                            // Redireciona para a MainActivity
                            startActivity(Intent(this@PreSignIn, MainActivity::class.java))
                            finish()  // Fecha a atividade atual
                        } else {
                            showToast("Credenciais erradas!")  // Exibe mensagem de erro
                        }
                    }
                } else {
                    // Mensagem de erro no caso de falha na requisição
                    withContext(Dispatchers.Main) {
                        println("Erro ao conectar ao servidor: Código $responseCode")
                    }
                }
                connection.disconnect()  // Fecha a conexão
            } catch (e: Exception) {
                // Trata exceções e exibe uma mensagem de erro
                withContext(Dispatchers.Main) {
                    println("Erro: ${e.message}")
                }
                e.printStackTrace()
            }
        }
    }
}
