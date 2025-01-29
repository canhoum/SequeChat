package pt.ipt.dam.sequechat.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        // Implementar lógica de cadastro aqui
    }

    private fun isValidSignUpDetails(): Boolean {
        return when {
            encodedImage == null -> {
                showToast("Select profile image")
                false
            }
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
            else -> true
        }
    }
}

