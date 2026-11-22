package com.example.infinitycomputers

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.infinitycomputers.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        configurarBiometria()

        if (verificarDisponibilidadBiometrica(mostrarMensaje = false)) {
            biometricPrompt.authenticate(promptInfo)
        }

        binding.btnLoginHuella.setOnClickListener {
            if (verificarDisponibilidadBiometrica(mostrarMensaje = true)) {
                biometricPrompt.authenticate(promptInfo)
            }
        }

        binding.btnLoginCorreo.setOnClickListener {
            val correo = binding.etCorreoLogin.text.toString().trim()
            val password = binding.etPasswordLogin.text.toString().trim()

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                        irAlPanelPrincipal()
                    } else {
                        Toast.makeText(
                            this,
                            "Error al iniciar sesión: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    private fun configurarBiometria() {
        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    val mensaje = when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON ->
                            "Huella desactivada por el usuario."
                        BiometricPrompt.ERROR_LOCKOUT ->
                            "Demasiados intentos fallidos. Sensor bloqueado temporalmente (30s)."
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT ->
                            "Sensor bloqueado por seguridad. Usa tu correo y contraseña."
                        else -> "Error biométrico ($errorCode): $errString"
                    }
                    binding.tvEstadoBiometria.text = mensaje
                    Toast.makeText(applicationContext, mensaje, Toast.LENGTH_LONG).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Bienvenido", Toast.LENGTH_SHORT).show()
                    irAlPanelPrincipal()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    val msg = "Huella no reconocida. Intenta nuevamente."
                    binding.tvEstadoBiometria.text = msg
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso a InfinityComputers")
            .setSubtitle("Confirma tu identidad con tu huella dactilar")
            .setNegativeButtonText("Usar correo y contraseña")
            .build()
    }

    private fun verificarDisponibilidadBiometrica(mostrarMensaje: Boolean): Boolean {
        val biometricManager = BiometricManager.from(this)
        val estado = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)

        val mensaje = when (estado) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                binding.tvEstadoBiometria.text = "Sensor listo. Toca el botón para usar tu huella."
                return true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                "Este dispositivo no cuenta con sensor de huella dactilar."
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                "El sensor biométrico no está disponible en este momento."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                "No hay huellas registradas en el teléfono. Configura una en Ajustes."
            else ->
                "La autenticación biométrica no está disponible."
        }

        binding.tvEstadoBiometria.text = mensaje
        if (mostrarMensaje) {
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        }
        return false
    }

    private fun irAlPanelPrincipal() {
        val intent = Intent(this, PanelActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}