package com.huertaonline.app.ui.carrito

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.huertaonline.app.data.local.CarritoItem
import com.huertaonline.app.databinding.ItemCarritoBinding

// Organiza cómo se muestra cada fila del carrito, asegurando que el nombre, la cantidad
// y la imagen aparezcan en su lugar correspondiente.
class CarritoAdapter(
    // Acción que se ejecutará cuando el usuario pulse el botón de borrar.
    private val onEliminar: (CarritoItem) -> Unit,
    // Acción que se ejecutará al modificar la cantidad.
    private val onActualizarCantidad: (CarritoItem, Int) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.VH>() {

    // Lista donde guardamos temporalmente los productos que vamos a mostrar.
    private var lista: List<CarritoItem> = emptyList()

    // Clase interna que sujeta los elementos visuales de cada fila (diseño de la celda).
    inner class VH(val b: ItemCarritoBinding) : RecyclerView.ViewHolder(b.root)

    // Crea el diseño de una fila nueva cuando la lista lo necesita.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    // Indica cuántos productos hay en total en el carrito.
    override fun getItemCount() = lista.size

    // Conecta los datos de un producto concreto con los textos e imágenes de la pantalla.
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = lista[position]
        holder.b.apply {
            // Asigna el nombre y la cantidad.
            tvNombre.text    = item.nombre
            tvCantidad.text  = item.cantidad.toString()

            // Calcula el precio total de esa línea y le añade el símbolo del euro.
            tvSubtotal.text  = "${"%.2f".format(item.precio * item.cantidad)}€"

            // Carga la foto del producto desde internet.
            Glide.with(root.context).load(item.imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivProducto)

            // Configura los botones de más y menos para actualizar la cantidad.
            btnMas.setOnClickListener {
                onActualizarCantidad(item, item.cantidad + 1)
            }
            btnMenos.setOnClickListener {
                if (item.cantidad > 1) {
                    onActualizarCantidad(item, item.cantidad - 1)
                }
            }

            // Configura el botón de la papelera para que avise de que este producto debe borrarse.
            btnEliminar.setOnClickListener { onEliminar(item) }
        }
    }

    // Función para refrescar la lista entera cuando el usuario añade o quita algo.
    fun actualizar(nueva: List<CarritoItem>) {
        lista = nueva
        notifyDataSetChanged() // Avisa a la pantalla para que se vuelva a dibujar.
    }
}