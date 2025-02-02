package pt.ipt.dam.sequechat.activities


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sequechat.databinding.ActivityAboutBinding

// Classe responsável pela exibição de About
class About : AppCompatActivity() {

    // Variável de binding para acessar os elementos da interface definidos no XML
    private lateinit var binding: ActivityAboutBinding

    // Método chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater) //carrega o layout associado a esta atividade utilizando view binding
        setContentView(binding.root)

        // botão para voltar à MainActivity
        binding.imageBack.setOnClickListener {
            finish() // Fecha a AboutActivity

        }
    }
}
