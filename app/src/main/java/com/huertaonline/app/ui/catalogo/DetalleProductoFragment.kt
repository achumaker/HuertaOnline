package com.huertaonline.app.ui.catalogo

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.huertaonline.app.data.model.Producto
import com.huertaonline.app.databinding.FragmentDetalleProductoBinding
import com.huertaonline.app.ui.carrito.CarritoViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

// Busca toda la información de un producto específico
// en la nube y presentarla de forma atractiva y organizada.
class DetalleProductoFragment : Fragment() {

    private var _binding: FragmentDetalleProductoBinding? = null
    private val binding get() = _binding!!

    // Conexión con el motor del carrito para permitir compras desde esta pantalla.
    private val carritoVm: CarritoViewModel by activityViewModels()

    // Recupera el código del producto que se pasó desde la pantalla anterior.
    private val productoId: String by lazy {
        arguments?.getString("productoId") ?: ""
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentDetalleProductoBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Al entrar en la pantalla, busca en internet los datos actualizados de este producto.
        viewLifecycleOwner.lifecycleScope.launch {
            val doc = FirebaseFirestore.getInstance()
                .collection("productos")
                .document(productoId)
                .get().await()

            // Convierte la respuesta del servidor en un objeto "Producto".
            val producto = doc.toObject(Producto::class.java)
                ?.copy(id = doc.id) ?: return@launch

            // Una vez tenemos los datos, los enviamos a la función encargada de pintarlos.
            mostrar(producto)
        }
    }

    // Función que coloca cada dato en su lugar correspondiente del diseño.
    private fun mostrar(p: Producto) {
        binding.apply {
            tvNombre.text      = p.nombre
            tvPrecio.text      = "${p.precio}€ / ${p.unidad}" // Ejemplo: 2.50€ / kg
            tvDescripcion.text = p.descripcion
            tvProductor.text   = "Vendido por: ${p.productorNombre}"
            tvStock.text       = "Stock: ${p.stock} ${p.unidad}"

            // Pone la primera letra de la categoría en mayúsculas (ej: "verduras" -> "Verduras").
            tvCategoria.text   = p.categoria.replaceFirstChar { it.uppercase() }

            // Muestra la nota media visual con estrellas y texto descriptivo.
            rbDetalle.rating   = p.mediaValoracion.toFloat()
            tvValoracion.text  = if (p.mediaValoracion > 0)
                "%.1f / 5".format(p.mediaValoracion) else "Sin valoraciones"

            // Carga la foto principal del artículo.
            Glide.with(requireContext()).load(p.imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery).into(ivProducto)

            // Configura el botón principal para añadir el artículo a la cesta.
            btnAgregar.setOnClickListener {
                carritoVm.agregar(p)
                Toast.makeText(requireContext(),
                    "Añadido al carrito ✓", Toast.LENGTH_SHORT).show()
            }

            // Botón para retroceder y volver al catálogo.
            btnVolver.setOnClickListener { findNavController().popBackStack() }
        }
    }

    // Limpia la memoria de la interfaz al cerrar la ficha.
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}