package com.example.infinitycomputers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.infinitycomputers.databinding.ActivitySensoresCampoBinding
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.sqrt

class SensoresCampoActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivitySensoresCampoBinding
    private lateinit var sensorManager: SensorManager

    private var acelerometro: Sensor? = null
    private var magnetometro: Sensor? = null

    private var lecturasActivas = true
    private var gravedadActual: FloatArray? = null
    private var geomagneticoActual: FloatArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySensoresCampoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometro = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        verificarDisponibilidadSensores()
        configurarBotones()
    }

    private fun verificarDisponibilidadSensores() {
        if (magnetometro == null) {
            binding.tvEstadoBrujula.text = getString(R.string.estado_brujula_no_disp)
            binding.tvEstadoBrujula.setBackgroundColor(
                ContextCompat.getColor(this, R.color.sensor_alerta_fondo)
            )
            binding.tvEstadoBrujula.setTextColor(
                ContextCompat.getColor(this, R.color.sensor_alerta_texto)
            )
        }
    }

    private fun configurarBotones() {
        binding.btnToggleSensores.setOnClickListener {
            lecturasActivas = !lecturasActivas
            if (lecturasActivas) {
                binding.btnToggleSensores.text = getString(R.string.btn_pausar_sensores)
                registrarListeners()
            } else {
                binding.btnToggleSensores.text = getString(R.string.btn_reanudar_sensores)
                sensorManager.unregisterListener(this)
            }
        }

        binding.btnVolverPanelSensores.setOnClickListener {
            finish()
        }
    }

    private fun registrarListeners() {
        acelerometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onResume() {
        super.onResume()
        if (lecturasActivas) registrarListeners()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !lecturasActivas) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val copia = event.values.clone()
                gravedadActual = copia

                val x = copia[0]
                val y = copia[1]
                val z = copia[2]

                binding.tvTelemetriaEjes.text = getString(R.string.formato_ejes_xyz, x, y, z)

                // Cálculo de inclinación respecto a la vertical/horizontal del equipo
                val horizontal = sqrt(x * x + y * y)
                val inclinacionGrados = Math.toDegrees(atan2(horizontal.toDouble(), kotlin.math.abs(z).toDouble()))

                binding.tvValorInclinacion.text = String.format(Locale.US, "%.1f°", inclinacionGrados)
                actualizarSemaforoNivel(inclinacionGrados)
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagneticoActual = event.values.clone()
            }
        }

        actualizarAzimut()
    }

    private fun actualizarSemaforoNivel(grados: Double) {
        val (fondoRes, textoRes, mensajeRes) = when {
            grados <= 5.0 -> Triple(
                R.color.sensor_ok_fondo,
                R.color.sensor_ok_texto,
                R.string.estado_nivel_optimo
            )
            grados <= 15.0 -> Triple(
                R.color.sensor_alerta_fondo,
                R.color.sensor_alerta_texto,
                R.string.estado_nivel_precaucion
            )
            else -> Triple(
                R.color.sensor_peligro_fondo,
                R.color.sensor_peligro_texto,
                R.string.estado_nivel_critico
            )
        }

        val colorFondo = ContextCompat.getColor(this, fondoRes)
        val colorTexto = ContextCompat.getColor(this, textoRes)

        binding.tvBadgeEstadoNivel.text = getString(mensajeRes)
        binding.tvBadgeEstadoNivel.setBackgroundColor(colorFondo)
        binding.tvBadgeEstadoNivel.setTextColor(colorTexto)
        binding.tvValorInclinacion.setTextColor(colorTexto)
        binding.cardNivelMastil.strokeColor = colorTexto
    }

    private fun actualizarAzimut() {
        val g = gravedadActual ?: return
        val m = geomagneticoActual ?: return

        val rotacion = FloatArray(9)
        val inclinacion = FloatArray(9)
        if (SensorManager.getRotationMatrix(rotacion, inclinacion, g, m)) {
            val orientacion = FloatArray(3)
            SensorManager.getOrientation(rotacion, orientacion)
            var azimut = Math.toDegrees(orientacion[0].toDouble()).toFloat()
            if (azimut < 0f) azimut += 360f

            val cardinal = obtenerPuntoCardinal(azimut)
            binding.tvValorAzimut.text = String.format(Locale.US, "%.0f° %s", azimut, cardinal)
        }
    }

    private fun obtenerPuntoCardinal(grados: Float): String {
        val direcciones = arrayOf("N", "NE", "E", "SE", "S", "SO", "O", "NO")
        val indice = (((grados + 22.5f) % 360f) / 45f).toInt()
        return direcciones[indice.coerceIn(0, 7)]
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}