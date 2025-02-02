package pt.ipt.dam.sequechat.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sequechat.databinding.ActivityAboutBinding
import pt.ipt.dam.sequechat.models.MainActivity

class About : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar o clique no botão para voltar à MainActivity
        binding.imageBack.setOnClickListener {
            finish()

        }
    }
}
