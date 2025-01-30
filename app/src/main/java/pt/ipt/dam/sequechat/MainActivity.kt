package pt.ipt.dam.sequechat
import android.annotation.SuppressLint
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.*
import org.json.JSONObject
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.OutputStreamWriter

class MainActivity : AppCompatActivity() {
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        // Receber os dados do Intent
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")

        var email_login = intent.getStringExtra("email_login")
        var password_login = intent.getStringExtra("password_login")

        if(email_login != null && password_login != null){
            checkLoginToSheety(email_login,password_login)
        }else{
            println("Erro ao receber os dados de login")
        }

        // Verificar se os dados não são nulos e chamar a função
        if (name != null && email != null && password != null) {
            postToSheety(name, email, password)
        } else {
            println("Erro ao receber os dados.")
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val button: Button = findViewById(R.id.btnteste)
            button.setOnClickListener{
                fetchSheetyData()
            }

    }
    fun fetchSheetyData() {
        val url = "https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha1"

        // Usamos coroutines para evitar bloqueios na UI
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }

                    // Converte a resposta para JSON
                    val jsonObject = JSONObject(response)
                    val folha1 = jsonObject.getJSONArray("folha1")

                    // Log dos dados obtidos
                    withContext(Dispatchers.Main) {
                        Log.d("batata frita","dados da api: $folha1")
                        println("Dados da API: $folha1")
                    }
                } else {
                    Log.d("batata frita","Erro na requisição: Código $responseCode")
                    println("Erro na requisição: Código $responseCode")
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun postToSheety(name: String, email: String, password: String) {
        val url = "https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha1"

        // Corpo da requisição (JSON)
        val jsonBody = JSONObject()
        val folha1 = JSONObject()

        // Adiciona os dados ao objeto JSON (substitui pelos teus campos)
        folha1.put("nome", name)
        folha1.put("email", email)
        folha1.put("password", password)

        jsonBody.put("folha1", folha1)

        // Usa coroutines para não bloquear a UI
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlObj = URL(url)
                val connection = urlObj.openConnection() as HttpURLConnection

                // Configurar a requisição
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                // Escrever o corpo da requisição
                val outputStreamWriter = OutputStreamWriter(connection.outputStream)
                outputStreamWriter.write(jsonBody.toString())
                outputStreamWriter.flush()

                println("AAAAAAAAAAAAAAAAA SIM")
                // Ler resposta
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    withContext(Dispatchers.Main) {
                        println("Resposta da API: $jsonResponse")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        println("Erro na requisição: Código $responseCode")
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
                            println("Login bem-sucedido!")
                            // Redireciona para a nova atividade (substituir pela tua view final)
                            //startActivity(Intent(this@MainActivity, SomeOtherActivity::class.java))
                            //finish() // Fecha a MainActivity
                        } else {
                            println("Email ou password incorretos.")
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