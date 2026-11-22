package com.example.infinitycomputers

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.infinitycomputers.databinding.ActivityPanelBinding
import com.google.firebase.auth.FirebaseAuth

class PanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPanelBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainPanel) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        binding.rvClientesWisp.layoutManager = LinearLayoutManager(this)

        binding.btnCrudClientes.setOnClickListener {
            Toast.makeText(this, "CRUD de clientes WISP", Toast.LENGTH_SHORT).show()
        }

        binding.btnHerramientasCampo.setOnClickListener {
            Toast.makeText(this, "Herramientas en campo (sensores)", Toast.LENGTH_SHORT).show()
        }

        binding.btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        binding.btnCrudClientes.setOnClickListener {
            startActivity(Intent(this, CrudClientesActivity::class.java))
        }
    }
}