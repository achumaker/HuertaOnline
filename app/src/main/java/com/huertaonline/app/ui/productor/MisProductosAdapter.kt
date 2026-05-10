package com.huertaonline.app.ui.productor

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.huertaonline.app.data.model.Producto
import com.huertaonline.app.databinding.ItemMiProductoBinding

// Organiza la información de los productos desde el punto de vista del negocio,
// resaltando el stock disponible y ofreciendo la opción de retirar artículos del catálogo.
class MisProductosAdapter(
    // Acción que se dispara al pulsar el botón de borrar (recibe el código del producto).
    private val onEliminar: (String) -> Unit
) : RecyclerView.Adapter<MisProductosAdapter.VH>() {

    // Almacén de los productos que pertenecen al agricultor actual.
    private var lista: List<Producto> = emptyList()

    // Clase que prepara y sujeta los elementos visuales de cada fila de la lista.
    inner class VH(val b: ItemMiProductoBinding) : RecyclerView.ViewHolder(b.root)

    // Crea el diseño de la fila cada vez que la lista necesita mostrar un producto más.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemMiProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    // Indica el número total de productos que el productor tiene registrados.
    override fun getItemCount() = lista.size

    // Rellena la fila con los datos de gestión: nombre, precio de venta y existencias.
    override fun onBindViewHolder(holder: VH, pos: Int) {
        val p = lista[pos]
        holder.b.apply {
            // Muestra el nombre comercial del artículo.
            tvNombre.text  = p.nombre

            // Muestra el precio configurado y su unidad (ej: 1.20€ / kg).
            tvPrecio.text  = "${p.precio}€ / ${p.unidad}"

            // Muestra de forma destacada cuántas unidades quedan en el almacén.
            tvStock.text   = "Stock: ${p.stock}"

            // Carga la foto del producto para que el productor lo identifique visualmente rápido.
            Glide.with(root.context).load(p.imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery).into(ivProducto)

            // Configura el botón de eliminar para que avise al sistema de qué producto debe quitar.
            btnEliminar.setOnClickListener { onEliminar(p.id) }
        }
    }

    // Refresca la pantalla cuando el productor añade, modifica o elimina un artículo.
    fun actualizar(nueva: List<Producto>) {
        lista = nueva
        notifyDataSetChanged()
    }
}