package pt.ipt.dam.sequechat.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sequechat.models.MainActivity
import pt.ipt.dam.sequechat.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private var encodedImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
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
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("name", binding.inputName.text.toString().trim())
                    putExtra("email", binding.inputEmail.text.toString().trim())
                    putExtra("password", binding.inputPassword.text.toString().trim())
                }
                startActivity(intent)
                true
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