package pt.ipt.dam.sequechat.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.YuvImage
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pt.ipt.dam.sequechat.models.MainActivity
import pt.ipt.dam.sequechat.databinding.ActivitySignUpBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignUpActivity : AppCompatActivity() {
    private lateinit var photoFile: File
    private lateinit var currentPhotoPath: String
    private lateinit var binding: ActivitySignUpBinding
    private val PICK_IMAGE_REQUEST = 1
    private var byteArrayImage: ByteArray = ByteArray(1)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        binding.imageProfile.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
    }

    // Verifica se a permissão da câmara foi concedida
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Pede permissão ao utilizador para usar a câmara
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Permissão da câmara negada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Abre a câmara para capturar uma imagem
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                // Criar um ficheiro temporário para armazenar a foto
                photoFile = createImageFile()
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "pt.ipt.dam.sequechat.provider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraLauncher.launch(takePictureIntent)
            } catch (ex: IOException) {
                Toast.makeText(this, "Erro ao criar o ficheiro de imagem", Toast.LENGTH_SHORT).show()
            }

    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Carregar a imagem na RoundedImageView
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(photoFile))
            binding.imageProfile.setImageBitmap(bitmap)
            byteArrayImage = convertBitmapToByteArray(bitmap)
        }
    }

    // Criar o ficheiro temporário para armazenar a foto
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",  /* prefixo */
            ".jpg",  /* sufixo */
            storageDir  /* diretório */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    // Processar a imagem selecionada
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            try {
                // Converter a imagem para bitmap e exibir na RoundedImageView
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                binding.imageProfile.setImageBitmap(bitmap)


            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Erro ao selecionar imagem", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)  // Compressão sem perda de qualidade
        return outputStream.toByteArray()
    }


    private fun setListeners() {
        binding.textcontacriada.setOnClickListener {
            startActivity(Intent(applicationContext, SignInActivity::class.java))
        }

        binding.buttonRegistar.setOnClickListener {
            if (isValidSignUpDetails()) {
                loading(true)  // Mostra o progress bar
                signUp()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        // Implementar lógica de cadastro aqui
        showToast("Sign Up process started!")

        // Simular um delay para teste (exemplo de 2 segundos)
        binding.root.postDelayed({
            loading(false) // Esconde o progress bar após o registo
            showToast("Sign Up process completed!")
        }, 2000)
    }

    private fun isValidSignUpDetails(): Boolean {
        return when {
            /*encodedImage == null -> {
                showToast("Select profile image")
                false
            }*/
            binding.inputName.text.toString().trim().isEmpty() -> {
                showToast("Enter name")
                false
            }
            binding.inputUsername.text.toString().trim().isEmpty() -> {
                showToast("Enter Username")
                false
            }
            binding.inputEmail.text.toString().trim().isEmpty() -> {
                showToast("Enter email")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches() -> {
                showToast("Enter valid email")
                false
            }
            binding.inputPassword.text.toString().trim().isEmpty() -> {
                showToast("Enter password")
                false
            }
            binding.inputConfirmarPassword.text.toString().trim().isEmpty() -> {
                showToast("Confirm your password")
                false
            }
            binding.inputPassword.text.toString() != binding.inputConfirmarPassword.text.toString() -> {
                showToast("Password & confirm password must be the same")
                false
            }

            else -> {
                // Cria um Intent para abrir a MainActivity e enviar os dados
                val name = binding.inputName.text.toString().trim()
                val email = binding.inputEmail.text.toString().trim()
                val password = binding.inputPassword.text.toString().trim()
                val username = binding.inputUsername.text.toString().trim()
                postToSheety(name,email,password,username,byteArrayImage)
                val intent = Intent(this, MainActivity::class.java).apply {
                }
                startActivity(intent)
                true
            }
        }
    }

    fun byteArrayToBase64String(byteArray: ByteArray): String {
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    private fun postToSheety(name: String, email: String, password: String, username: String, image: ByteArray) {
        val url = "https://api.sheety.co/182b17ec2dcc0a8d3be919b2baff9dfc/sequechat/folha1"
        val imageConverted = byteArrayToBase64String(image)
        val jsonBody = JSONObject().apply {
            put("folha1", JSONObject().apply {
                put("nome", name)
                put("email", email)
                put("password", password)
                put("username", username)
                put("image", imageConverted)
            })
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlObj = URL(url)
                val connection = urlObj.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                OutputStreamWriter(connection.outputStream).use { it.write(jsonBody.toString()) }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    withContext(Dispatchers.Main) {
                        Log.d("MainActivity", "Resposta da API: $response")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.d("MainActivity", "Erro na requisição: Código $responseCode")
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonRegistar.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonRegistar.visibility = View.VISIBLE
        }
    }
}