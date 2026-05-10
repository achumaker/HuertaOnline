package com.huertaonline.app.ui.pedidos

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.huertaonline.app.data.model.Pedido
import com.huertaonline.app.databinding.ItemPedidoBinding
import java.text.SimpleDateFormat
import java.util.*

// Define cómo se muestra la información de cada compra, aplicando formatos de fecha,
// moneda y colores diferentes según el estado del envío.
class PedidoAdapter : RecyclerView.Adapter<PedidoAdapter.VH>() {

    // Lista que contiene todos los pedidos que vamos a mostrar.
    private var lista: List<Pedido> = emptyList()

    // Herramienta para convertir la fecha del sistema en un formato legible (día/mes/año hora:minutos).
    private val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // Clase que sujeta los elementos visuales de la tarjeta del pedido.
    inner class VH(val b: ItemPedidoBinding) : RecyclerView.ViewHolder(b.root)

    // Crea el diseño de la tarjeta cuando la lista necesita mostrar una nueva fila.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    // Indica el número total de pedidos que hay en el historial.
    override fun getItemCount() = lista.size

    // Conecta los datos de un pedido concreto con los textos y colores de la interfaz.
    override fun onBindViewHolder(holder: VH, pos: Int) {
        val p = lista[pos]
        holder.b.apply {
            // Convierte la fecha técnica en un texto comprensible.
            tvFecha.text   = fmt.format(Date(p.fecha))

            // Muestra el importe total con dos decimales y el símbolo del euro.
            tvTotal.text   = "${"%.2f".format(p.total)}€"

            // Muestra el estado (Pendiente, Enviado...) con la primera letra en mayúscula.
            tvEstado.text  = p.estado.replaceFirstChar { it.uppercase() }

            // Crea un resumen rápido de los productos comprados (ej: Tomate x2, Manzana x1).
            tvItems.text   = p.items.joinToString(", ") { "${it.nombre} x${it.cantidad}" }

            // Cambia el color del texto del estado para que sea más intuitivo:
            // Verde para entregado, azul para enviado, naranja para preparación y gris para el resto.
            tvEstado.setTextColor(when(p.estado) {
                "entregado"  -> root.context.getColor(android.R.color.holo_green_dark)
                "enviado"    -> root.context.getColor(android.R.color.holo_blue_dark)
                "preparando" -> root.context.getColor(android.R.color.holo_orange_dark)
                else         -> root.context.getColor(android.R.color.darker_gray)
            })
        }
    }

    // Actualiza la lista completa y refresca la pantalla cuando hay cambios en los pedidos.
    fun actualizar(nueva: List<Pedido>) {
        lista = nueva
        notifyDataSetChanged()
    }
}