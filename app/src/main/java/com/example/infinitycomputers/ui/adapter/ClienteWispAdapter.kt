package com.example.infinitycomputers.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.infinitycomputers.data.ClienteWisp
import com.example.infinitycomputers.databinding.ItemClienteWispBinding

class ClienteWispAdapter(
    private val listaClientes: MutableList<ClienteWisp>,
    private val onClienteClick: (ClienteWisp) -> Unit
) : RecyclerView.Adapter<ClienteWispAdapter.ClienteViewHolder>() {

    inner class ClienteViewHolder(val binding: ItemClienteWispBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val binding = ItemClienteWispBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = listaClientes[position]
        with(holder.binding) {
            tvIdCliente.text = "ID Cliente: ${cliente.idCliente}"
            tvNombreCompleto.text = cliente.nombreCompleto
            tvTelefono.text = "Teléfono: ${cliente.telefono}"
            tvPlanMensual.text = "Plan mensual: ${cliente.planMensual}"
            tvLocalidad.text = "Localidad: ${cliente.localidad}"
            tvDireccionIp.text = "Dirección IP: ${cliente.direccionIp}"
            tvEstadoServicio.text = "Estado: ${cliente.estadoServicio}"

            root.setOnClickListener {
                onClienteClick(cliente)
            }
        }
    }

    override fun getItemCount(): Int = listaClientes.size

    fun actualizarLista(nuevaLista: List<ClienteWisp>) {
        listaClientes.clear()
        listaClientes.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}