package com.example.infinitycomputers

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.infinitycomputers.databinding.ActivityRegistrarTecnicoBinding
import com.google.firebase.auth.FirebaseAuth

class RegistrarTecnicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarTecnicoBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarTecnicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnCrearCuentaTecnico.setOnClickListener {
            validarYRegistrarTecnico()
        }

        binding.btnVolverPanelTecnico.setOnClickListener {
            finish()
        }
    }

    private fun validarYRegistrarTecnico() {
        val correo = binding.etCorreoTecnico.text.toString().trim()
        val password = binding.etPasswordTecnico.text.toString()
        val confirmarPassword = binding.etConfirmarPasswordTecnico.text.toString()

        if (correo.isEmpty()) {
            binding.etCorreoTecnico.error = getString(R.string.error_correo_vacio)
            binding.etCorreoTecnico.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.etCorreoTecnico.error = getString(R.string.error_correo_invalido)
            binding.etCorreoTecnico.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.etPasswordTecnico.error = getString(R.string.error_password_corta)
            binding.etPasswordTecnico.requestFocus()
            return
        }

        if (password != confirmarPassword) {
            binding.etConfirmarPasswordTecnico.error = getString(R.string.error_password_no_coincide)
            binding.etConfirmarPasswordTecnico.requestFocus()
            return
        }

        binding.btnCrearCuentaTecnico.isEnabled = false

        auth.createUserWithEmailAndPassword(correo, password)
            .addOnSuccessListener {
                binding.btnCrearCuentaTecnico.isEnabled = true
                Toast.makeText(
                    this,
                    getString(R.string.msg_tecnico_registrado),
                    Toast.LENGTH_SHORT
                ).show()
                limpiarFormulario()
                finish()
            }
            .addOnFailureListener { exception ->
                binding.btnCrearCuentaTecnico.isEnabled = true
                Toast.makeText(
                    this,
                    exception.localizedMessage ?: "Error al registrar técnico",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun limpiarFormulario() {
        binding.etCorreoTecnico.text?.clear()
        binding.etPasswordTecnico.text?.clear()
        binding.etConfirmarPasswordTecnico.text?.clear()
    }
}