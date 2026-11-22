package com.example.infinitycomputers

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.infinitycomputers.data.ClienteWisp
import com.example.infinitycomputers.databinding.ActivityPanelBinding
import com.example.infinitycomputers.ui.adapter.ClienteWispAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class PanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPanelBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ClienteWispAdapter
    private var clientesListener: ListenerRegistration? = null

    private val listaClientes = mutableListOf<ClienteWisp>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        configurarRecyclerView()
        configurarBotones()
        escucharClientesEnTiempoReal()
    }

    private fun configurarRecyclerView() {
        adapter = ClienteWispAdapter(listaClientes) { clienteSeleccionado ->
            val intent = Intent(this, CrudClientesActivity::class.java).apply {
                putExtra("ID_CLIENTE_SELECCIONADO", clienteSeleccionado.idCliente)
            }
            startActivity(intent)
        }

        binding.rvClientesWisp.apply {
            layoutManager = LinearLayoutManager(this@PanelActivity)
            adapter = this@PanelActivity.adapter
        }
    }

    private fun configurarBotones() {
        binding.btnIrCrudClientes.setOnClickListener {
            startActivity(Intent(this, CrudClientesActivity::class.java))
        }

        binding.btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun escucharClientesEnTiempoReal() {
        clientesListener?.remove()

        // Asegúrate de que "clientes_wisp" coincida con la colección que usa CrudClientesActivity
        clientesListener = db.collection("clientes_wisp")
            .orderBy("nombreCompleto", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        "Error al cargar clientes: ${error.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val nuevosClientes = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(ClienteWisp::class.java)?.copy(
                            idCliente = doc.id.ifBlank {
                                doc.getString("idCliente") ?: ""
                            }
                        )
                    }

                    adapter.actualizarLista(nuevosClientes)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        clientesListener?.remove()
    }
}