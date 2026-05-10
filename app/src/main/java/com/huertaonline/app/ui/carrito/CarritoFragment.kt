package com.huertaonline.app.ui.carrito

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.huertaonline.app.R
import com.huertaonline.app.databinding.FragmentCarritoBinding

// Gestiona la lógica visual de la cesta de la compra, alternando entre la lista de productos
// y un aviso de "carrito vacío", según corresponda.
class CarritoFragment : Fragment() {

    // Referencia a los elementos visuales de la pantalla.
    private var _binding: FragmentCarritoBinding? = null
    private val binding get() = _binding!!

    // Conexión con los datos del carrito (compartida con otras partes de la app).
    private val vm: CarritoViewModel by activityViewModels()

    // El encargado de dibujar la lista de productos.
    private lateinit var adapter: CarritoAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentCarritoBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Configuramos el adaptador y le decimos que, si se pulsa borrar,
        // avise al motor de datos para quitar ese producto.
        adapter = CarritoAdapter { item -> vm.eliminar(item) }

        // Preparamos la lista visual (RecyclerView) para que se muestre en vertical.
        binding.recyclerCarrito.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CarritoFragment.adapter
        }

        // Vigilamos la lista de productos. Si cambia (se añade o quita algo):
        vm.items.observe(viewLifecycleOwner) { lista ->
            adapter.actualizar(lista) // Refrescamos los elementos visuales.

            // Si no hay productos, mostramos un mensaje de "Carrito vacío".
            binding.layoutVacio.visibility =
                if (lista.isEmpty()) View.VISIBLE else View.GONE
            // Si hay productos, mostramos la lista y el botón de compra.
            binding.layoutContenido.visibility =
                if (lista.isEmpty()) View.GONE else View.VISIBLE
        }

        // Vigilamos el precio total para actualizar el texto de la pantalla automáticamente.
        vm.total.observe(viewLifecycleOwner) { total ->
            binding.tvTotal.text = "Total: ${"%.2f".format(total)}€"
        }

        // Al pulsar el botón de finalizar compra, navegamos a la pantalla de confirmación.
        binding.btnCheckout.setOnClickListener {
            findNavController().navigate(R.id.action_carrito_to_checkout)
        }
    }

    // Limpieza de la memoria cuando el usuario sale de esta pantalla.
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}