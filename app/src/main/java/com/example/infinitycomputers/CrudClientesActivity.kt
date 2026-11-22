package com.example.infinitycomputers

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.infinitycomputers.data.ClienteWisp
import com.example.infinitycomputers.databinding.ActivityCrudClientesBinding
import com.google.firebase.firestore.FirebaseFirestore

class CrudClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrudClientesBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCrudClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainCrudClientes) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        // CREATE
        binding.btnGuardarCrud.setOnClickListener {
            guardarCliente()
        }

        // UPDATE
        binding.btnActualizarCrud.setOnClickListener {
            actualizarCliente()
        }

        // DELETE
        binding.btnEliminarCrud.setOnClickListener {
            eliminarCliente()
        }

        // LIMPIAR
        binding.btnLimpiarCrud.setOnClickListener {
            limpiarCampos()
        }
        binding.btnBuscarCrud.setOnClickListener {
            buscarCliente()
        }

        binding.btnVolverPanel.setOnClickListener {
            startActivity(Intent(this, PanelActivity::class.java))
            finish()
        }
    }



    private fun validarFormulario(requiereValidacionCompleta: Boolean): ClienteWisp? {
        val idCliente = binding.etIdCliente.text.toString().trim()
        if (idCliente.isEmpty()) {
            Toast.makeText(this, "Ingresa el ID del cliente", Toast.LENGTH_SHORT).show()
            return null
        }

        if (!requiereValidacionCompleta) {
            return ClienteWisp(idCliente = idCliente)
        }

        val nombreCompleto = binding.etNombreCompleto.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val planMensual = binding.etPlanMensual.text.toString().trim()
        val localidad = binding.etLocalidad.text.toString().trim()
        val direccionIp = binding.etDireccionIp.text.toString().trim()
        val estadoServicio = binding.etEstadoServicio.text.toString().trim().ifEmpty { "Activo" }

        if (nombreCompleto.isEmpty() || telefono.isEmpty() || planMensual.isEmpty() ||
            localidad.isEmpty() || direccionIp.isEmpty()
        ) {
            Toast.makeText(this, "Completa todos los campos del cliente", Toast.LENGTH_SHORT).show()
            return null
        }

        if (telefono.length < 10) {
            Toast.makeText(this, "El teléfono debe tener al menos 10 dígitos", Toast.LENGTH_SHORT).show()
            return null
        }

        if (!Patterns.IP_ADDRESS.matcher(direccionIp).matches()) {
            Toast.makeText(this, "Ingresa una dirección IP válida", Toast.LENGTH_SHORT).show()
            return null
        }

        return ClienteWisp(
            idCliente = idCliente,
            nombreCompleto = nombreCompleto,
            telefono = telefono,
            planMensual = planMensual,
            localidad = localidad,
            direccionIp = direccionIp,
            estadoServicio = estadoServicio
        )
    }

    private fun guardarCliente() {
        val cliente = validarFormulario(requiereValidacionCompleta = true) ?: return

        db.collection("clientes_wisp")
            .document(cliente.idCliente)
            .set(cliente)
            .addOnSuccessListener {
                Toast.makeText(this, "Cliente guardado", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun buscarCliente() {
        val idCliente = binding.etIdCliente.text.toString().trim()
        val nombreCompleto = binding.etNombreCompleto.text.toString().trim()

        if (idCliente.isEmpty() && nombreCompleto.isEmpty()) {
            Toast.makeText(this, "Ingresa el ID del cliente o el nombre completo para buscar", Toast.LENGTH_SHORT).show()
            return
        }

        if (idCliente.isNotEmpty()) {
            db.collection("clientes_wisp")
                .document(idCliente)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val cliente = doc.toObject(ClienteWisp::class.java)
                        cliente?.let { mostrarEnCampos(it) }
                        Toast.makeText(this, "Cliente encontrado por ID", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "No existe un cliente con el ID: $idCliente", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al buscar por ID: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        } else {
            db.collection("clientes_wisp")
                .whereEqualTo("nombreCompleto", nombreCompleto)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val cliente = querySnapshot.documents.first().toObject(ClienteWisp::class.java)
                        cliente?.let { mostrarEnCampos(it) }
                        val msg = if (querySnapshot.size() > 1) {
                            "Se encontraron ${querySnapshot.size()} coincidencias; se cargó la primera"
                        } else {
                            "Cliente encontrado por nombre"
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "No se encontró cliente con el nombre: $nombreCompleto", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al buscar por nombre: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun actualizarCliente() {
        val cliente = validarFormulario(requiereValidacionCompleta = true) ?: return

        val datosActualizados = mapOf(
            "idCliente" to cliente.idCliente,
            "nombreCompleto" to cliente.nombreCompleto,
            "telefono" to cliente.telefono,
            "planMensual" to cliente.planMensual,
            "localidad" to cliente.localidad,
            "direccionIp" to cliente.direccionIp,
            "estadoServicio" to cliente.estadoServicio
        )

        db.collection("clientes_wisp")
            .document(cliente.idCliente)
            .update(datosActualizados)
            .addOnSuccessListener {
                Toast.makeText(this, "Cliente actualizado", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun eliminarCliente() {
        val consulta = validarFormulario(requiereValidacionCompleta = false) ?: return

        db.collection("clientes_wisp")
            .document(consulta.idCliente)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun mostrarEnCampos(cliente: ClienteWisp) {
        binding.etIdCliente.setText(cliente.idCliente)
        binding.etNombreCompleto.setText(cliente.nombreCompleto)
        binding.etTelefono.setText(cliente.telefono)
        binding.etPlanMensual.setText(cliente.planMensual)
        binding.etLocalidad.setText(cliente.localidad)
        binding.etDireccionIp.setText(cliente.direccionIp)
        binding.etEstadoServicio.setText(cliente.estadoServicio)
    }

    private fun limpiarCampos() {
        binding.etIdCliente.text.clear()
        binding.etNombreCompleto.text.clear()
        binding.etTelefono.text.clear()
        binding.etPlanMensual.text.clear()
        binding.etLocalidad.text.clear()
        binding.etDireccionIp.text.clear()
        binding.etEstadoServicio.setText("Activo")
    }

}