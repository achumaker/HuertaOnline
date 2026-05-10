package com.huertaonline.app.ui.productor

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.huertaonline.app.R
import com.huertaonline.app.databinding.FragmentMisProductosBinding

// Conecta la base de datos de productos filtrada por el usuario actual con
// la interfaz, permitiendo una gestión ágil del catálogo propio.
class MisProductosFragment : Fragment() {

    private var _binding: FragmentMisProductosBinding? = null
    private val binding get() = _binding!!

    // Motor que contiene la lista de productos filtrada para este productor.
    private val vm: ProductorViewModel by viewModels()

    // El adaptador que dibuja las tarjetas de producto con opción de eliminar.
    private lateinit var adapter: MisProductosAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentMisProductosBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Configuramos el adaptador.
        // Si el productor pulsa eliminar, el ViewModel desactiva ese producto en la nube.
        adapter = MisProductosAdapter { productoId ->
            vm.desactivarProducto(productoId)
        }

        // Configuramos el RecyclerView en formato de lista vertical.
        binding.recyclerMisProductos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MisProductosFragment.adapter
        }

        // Observamos la lista de "mis productos".
        // Si el productor añade o quita algo, la pantalla se refresca sola.
        vm.misProductos.observe(viewLifecycleOwner) { lista ->
            adapter.actualizar(lista)

            // Si no tiene nada a la venta, mostramos un texto de ayuda.
            binding.tvVacio.visibility =
                if (lista.isEmpty()) View.VISIBLE else View.GONE
        }

        // Configuración del botón flotante (FAB) para ir a la pantalla de crear producto.
        binding.fabNuevoProducto.setOnClickListener {
            findNavController().navigate(R.id.action_misproductos_to_crear)
        }
    }

    // Limpieza de la vista al salir para evitar fugas de memoria.
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}