package com.huertaonline.app.ui.catalogo

import android.os.Bundle
import androidx.core.os.bundleOf
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.huertaonline.app.R
import com.huertaonline.app.databinding.FragmentCatalogoBinding
import com.huertaonline.app.ui.carrito.CarritoViewModel

// Gestiona la visualización de la tienda, permitiendo al usuario buscar artículos,
// filtrarlos por tipo y añadirlos directamente a su compra.
class CatalogoFragment : Fragment() {

    private var _binding: FragmentCatalogoBinding? = null
    private val binding get() = _binding!!

    // Motor para gestionar la lógica del catálogo (búsquedas y filtros).
    private val vm: CatalogoViewModel by viewModels()
    // Motor del carrito para añadir productos desde aquí mismo.
    private val carritoVm: CarritoViewModel by activityViewModels()

    private lateinit var adapter: ProductoAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentCatalogoBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Prepara la lista, el buscador y los botones de categoría.
        configurarRecycler()
        configurarBuscador()
        configurarFiltros()

        // Vigila la lista de productos: si hay cambios, actualiza la pantalla.
        // Si no hay ninguno que mostrar, enseña un aviso de "Catálogo vacío".
        vm.productos.observe(viewLifecycleOwner) { lista ->
            adapter.actualizar(lista)
            binding.tvVacio.visibility =
                if (lista.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun configurarRecycler() {
        adapter = ProductoAdapter(
            // Acción 1: Si pulsa en el producto, abre la pantalla de detalles.
            onClick = { producto ->
                val bundle = bundleOf("productoId" to producto.id)
                findNavController().navigate(
                    R.id.nav_detalle_producto,
                    bundle
                )
            },
            // Acción 2: Si pulsa el botón del carrito, lo añade con la cantidad elegida.
            onAgregarCarrito = { producto, cantidad ->
                carritoVm.agregar(producto, cantidad)
                Toast.makeText(requireContext(),
                    "${producto.nombre} ($cantidad) añadido al carrito",
                    Toast.LENGTH_SHORT).show()
            }
        )
        // Organiza los productos en una cuadrícula de 2 columnas (tipo tienda online).
        binding.recyclerProductos.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@CatalogoFragment.adapter
        }
    }

    private fun configurarBuscador() {
        // Detecta cuando el usuario escribe en la barra de búsqueda.
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(texto: String?): Boolean {
                // Filtra la lista en tiempo real según lo que se vaya escribiendo.
                vm.filtrar(texto ?: "")
                vm.filtrados.observe(viewLifecycleOwner) { adapter.actualizar(it) }
                return true
            }
        })
    }

    private fun configurarFiltros() {
        // Configura los botones (chips) para filtrar por categorías específicas.
        binding.chipTodas.setOnClickListener    { vm.filtrar("", "todas");    vm.productos.observe(viewLifecycleOwner) { adapter.actualizar(it) } }
        binding.chipVerduras.setOnClickListener { vm.filtrar("", "verduras"); vm.filtrados.observe(viewLifecycleOwner) { adapter.actualizar(it) } }
        binding.chipFrutas.setOnClickListener   { vm.filtrar("", "frutas");   vm.filtrados.observe(viewLifecycleOwner) { adapter.actualizar(it) } }
        binding.chipConservas.setOnClickListener{ vm.filtrar("", "conservas");vm.filtrados.observe(viewLifecycleOwner) { adapter.actualizar(it) } }
    }

    // Libera la memoria de la interfaz al salir de la pantalla.
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}