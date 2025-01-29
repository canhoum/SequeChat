package pt.ipt.dam.sequechat
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
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
    fun postToSheety() {
        val url = "https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha1"

        // Corpo da requisição (JSON)
        val jsonBody = JSONObject()
        val folha1 = JSONObject()

        // Adiciona os dados ao objeto JSON (substitui pelos teus campos)
        folha1.put("nome", "inputName")
        folha1.put("email", "inputEmail")
        folha1.put("password", "inputName")
        folha1.put("password", "inputPassword")

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
}