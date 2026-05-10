package com.huertaonline.app.ui.productor

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.huertaonline.app.data.model.Pedido
import com.huertaonline.app.databinding.ItemPedidoBinding
import java.text.SimpleDateFormat
import java.util.*

class PedidoProductorAdapter(
    private val productorId: String,
    private val onCambiarEstado: (Pedido, String) -> Unit
) : RecyclerView.Adapter<PedidoProductorAdapter.VH>() {

    private var lista: List<Pedido> = emptyList()
    private val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class VH(val b: ItemPedidoBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val p = lista[pos]
        holder.b.apply {
            tvFecha.text   = fmt.format(Date(p.fecha))
            tvEstado.text  = p.estado.replaceFirstChar { it.uppercase() }

            // Mostramos el nombre del cliente y su dirección de envío
            tvTotal.text      = "Para: ${p.consumidorNombre}"
            tvDireccion.text  = "📍 Envío a: ${p.direccionEnvio}"
            tvDireccion.visibility = View.VISIBLE

            // Solo mostramos los productos que pertenecen a este productor
            val misItems = p.items.filter { it.productorId == productorId }
            tvItems.text   = misItems.joinToString(", ") { "${it.nombre} x${it.cantidad}" }

            // Calculamos el subtotal de lo que este productor va a cobrar
            val subtotalProductor = misItems.sumOf { it.cantidad * it.precioUnitario }
            tvTotal.text = "Para: ${p.consumidorNombre} (${"%.2f".format(subtotalProductor)}€)"

            tvEstado.setTextColor(when(p.estado) {
                "entregado"  -> root.context.getColor(android.R.color.holo_green_dark)
                "enviado"    -> root.context.getColor(android.R.color.holo_blue_dark)
                "preparando" -> root.context.getColor(android.R.color.holo_orange_dark)
                else         -> root.context.getColor(android.R.color.darker_gray)
            })

            // Configuración del botón de acción para el productor
            if (p.estado == "pendiente" || p.estado == "preparando") {
                btnAccion.visibility = View.VISIBLE
                btnAccion.text = "MARCAR ENVIADO"
                btnAccion.setOnClickListener {
                    androidx.appcompat.app.AlertDialog.Builder(root.context)
                        .setTitle("Confirmar envío")
                        .setMessage("¿Deseas marcar este pedido como ENVIADO?")
                        .setPositiveButton("Sí, enviado") { _, _ ->
                            onCambiarEstado(p, "enviado")
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            } else {
                btnAccion.visibility = View.GONE
            }

            // Quitamos el click en todo el pedido para evitar cambios accidentales
            root.setOnClickListener(null)
        }
    }

    fun actualizar(nueva: List<Pedido>) {
        lista = nueva
        notifyDataSetChanged()
    }
}
