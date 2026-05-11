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
import com.google.firebase.firestore.ListenerRegistration
import com.huertaonline.app.data.model.Producto
import com.huertaonline.app.databinding.FragmentDetalleProductoBinding
import com.huertaonline.app.ui.carrito.CarritoViewModel
import kotlinx.coroutines.*

class DetalleProductoFragment : Fragment() {

    private var _binding: FragmentDetalleProductoBinding? = null
    private val binding get() = _binding!!
    private val carritoVm: CarritoViewModel by activityViewModels()
    private var snapshotListener: ListenerRegistration? = null
    private var cantidadSeleccionada = 1

    private val productoId: String by lazy {
        arguments?.getString("productoId") ?: ""
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentDetalleProductoBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Escuchar cambios en el producto en tiempo real (para ver las estrellas al momento)
        snapshotListener = FirebaseFirestore.getInstance()
            .collection("productos").document(productoId)
            .addSnapshotListener { snapshot, _ ->
                val producto = snapshot?.toObject(Producto::class.java)?.copy(id = snapshot.id) ?: return@addSnapshotListener
                mostrar(producto)
            }
    }

    private fun mostrar(p: Producto) {
        binding.apply {
            tvNombre.text      = p.nombre
            tvPrecio.text      = "${p.precio}€ / ${p.unidad}"
            tvDescripcion.text = p.descripcion
            tvProductor.text   = "Vendido por: ${p.productorNombre}"
            tvStock.text       = "Stock: ${p.stock} ${p.unidad}"
            tvCategoria.text   = p.categoria.replaceFirstChar { it.uppercase() }

            // Actualización visual de estrellas
            rbDetalle.rating   = p.mediaValoracion.toFloat()
            tvValoracion.text  = if (p.mediaValoracion > 0)
                "%.1f / 5".format(p.mediaValoracion) else "Sin valoraciones aún"

            if (p.stock > 0) {
                btnAgregar.isEnabled = true
                btnAgregar.text = "Añadir al carrito"
                btnAgregar.alpha = 1.0f
            } else {
                btnAgregar.isEnabled = false
                btnAgregar.text = "AGOTADO"
                btnAgregar.alpha = 0.5f
                cantidadSeleccionada = 0
                tvCantidad.text = "0"
            }

            Glide.with(requireContext()).load(p.imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery).into(ivProducto)

            btnAgregar.setOnClickListener {
                carritoVm.agregar(p, cantidadSeleccionada)
                Toast.makeText(requireContext(), "¡${cantidadSeleccionada}x ${p.nombre} añadidos!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }

            btnMas.setOnClickListener {
                if (cantidadSeleccionada < p.stock) {
                    cantidadSeleccionada++
                    tvCantidad.text = cantidadSeleccionada.toString()
                }
            }
            btnMenos.setOnClickListener {
                if (cantidadSeleccionada > 1) {
                    cantidadSeleccionada--
                    tvCantidad.text = cantidadSeleccionada.toString()
                }
            }
            btnVolver.setOnClickListener { findNavController().popBackStack() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        _binding = null
    }
}
