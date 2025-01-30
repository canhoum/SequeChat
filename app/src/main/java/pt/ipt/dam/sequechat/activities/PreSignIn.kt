package pt.ipt.dam.sequechat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sequechat.MainActivity
import pt.ipt.dam.sequechat.databinding.ActivitySignInBinding

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
            Log.d("SignInActivity","AAAAAAAAAAAAAAAAA")
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Preencha todos os campos!")
            } else {
                // Enviar os dados de login para a MainActivity
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("email_login", email)
                    putExtra("password_login", password)
                }
                startActivity(intent)
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
}
