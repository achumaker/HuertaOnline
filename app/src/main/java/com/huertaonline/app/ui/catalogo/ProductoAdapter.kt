package com.huertaonline.app.ui.catalogo

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.huertaonline.app.R
import com.huertaonline.app.data.model.Producto
import com.huertaonline.app.databinding.ItemProductoBinding

// Plantilla que se repite para cada artículo, asegurando que la foto, el precio
// y el nombre se vean correctamente en el catálogo general.
class ProductoAdapter(
    // Acción que ocurre al tocar el producto para ver sus detalles.
    private val onClick: (Producto) -> Unit,
    // Acción que ocurre al tocar el botón rápido de añadir a la cesta.
    private val onAgregarCarrito: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    // Almacén temporal de los productos que se van a mostrar.
    private var lista: List<Producto> = emptyList()

    // Clase que mantiene la referencia a los elementos visuales de cada tarjeta.
    inner class ViewHolder(val b: ItemProductoBinding) :
        RecyclerView.ViewHolder(b.root)

    // Crea el diseño de una tarjeta nueva cuando el catálogo lo necesita.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    // Indica al sistema cuántos productos hay en total para mostrar.
    override fun getItemCount() = lista.size

    // Rellena la tarjeta con los datos reales del producto en su posición correspondiente.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = lista[position]
        holder.b.apply {
            // Asigna los textos principales: nombre, precio por unidad y productor.
            tvNombre.text         = p.nombre
            tvPrecio.text         = "${p.precio}€ / ${p.unidad}"
            tvProductor.text      = p.productorNombre

            // Muestra la puntuación con una estrella o un aviso si es nuevo.
            tvValoracion.text     = if (p.mediaValoracion > 0) "★ ${"%.1f".format(p.mediaValoracion)}" else "Sin valorar"

            // Pone la primera letra de la categoría en mayúscula por estética.
            tvCategoria.text      = p.categoria.replaceFirstChar { it.uppercase() }

            // Descarga y ajusta la imagen del producto para que encaje bien en el recuadro.
            Glide.with(root.context)
                .load(p.imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(ivProducto)

            // Configura qué pasa al tocar cualquier parte de la tarjeta (ir al detalle).
            root.setOnClickListener            { onClick(p) }
            // Configura qué pasa al tocar específicamente el botón de compra rápida.
            btnAgregarCarrito.setOnClickListener{ onAgregarCarrito(p) }
        }
    }

    // Actualiza el catálogo completo (por ejemplo, tras una búsqueda o filtrado).
    fun actualizar(nueva: List<Producto>) {
        lista = nueva
        notifyDataSetChanged() // Refresca la pantalla con los nuevos datos.
    }
}