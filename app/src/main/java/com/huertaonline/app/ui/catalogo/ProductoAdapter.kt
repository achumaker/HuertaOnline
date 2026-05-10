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
    // Acción que ocurre al tocar el botón rápido de añadir a la cesta con una cantidad.
    private val onAgregarCarrito: (Producto, Int) -> Unit
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

            // Muestra la puntuación visual con estrellas.
            rbProducto.rating     = p.mediaValoracion.toFloat()

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

            // Lógica del selector de cantidad local para esta tarjeta.
            var cantidadLocal = 1
            tvCantidad.text = "1"

            btnMas.setOnClickListener {
                cantidadLocal++
                tvCantidad.text = cantidadLocal.toString()
            }

            btnMenos.setOnClickListener {
                if (cantidadLocal > 1) {
                    cantidadLocal--
                    tvCantidad.text = cantidadLocal.toString()
                }
            }

            // Configura qué pasa al tocar específicamente el botón de compra rápida.
            btnAgregarCarrito.setOnClickListener {
                onAgregarCarrito(p, cantidadLocal)
                // Opcional: resetear a 1 después de añadir.
                cantidadLocal = 1
                tvCantidad.text = "1"
            }
        }
    }

    // Actualiza el catálogo completo (por ejemplo, tras una búsqueda o filtrado).
    fun actualizar(nueva: List<Producto>) {
        lista = nueva
        notifyDataSetChanged() // Refresca la pantalla con los nuevos datos.
    }
}