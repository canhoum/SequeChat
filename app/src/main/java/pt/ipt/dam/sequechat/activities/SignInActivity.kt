package pt.ipt.dam.sequechat.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sequechat.databinding.ActivitySignInBinding

// Classe responsável pela atividade de início de sessão (SignIn)
class SignInActivity : AppCompatActivity() {

    // Declaração do ViewBinding para associar os elementos da interface
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciação do ViewBinding para ligar o layout XML à atividade
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Chamar a função que define os listeners para os botões
        setListeners()
    }

    // Função para definir os eventos (listeners) associados aos elementos da interface
    private fun setListeners() {
        // Evento de clique no texto "Criar Nova Conta"
        binding.textCriarNovaConta.setOnClickListener {
            // Iniciar a atividade de registo (SignUpActivity)
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
        }
    }
}
