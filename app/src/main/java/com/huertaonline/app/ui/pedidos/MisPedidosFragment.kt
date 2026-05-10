package com.huertaonline.app.ui.pedidos

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.huertaonline.app.data.repository.AuthRepository
import com.huertaonline.app.data.repository.PedidoRepository
import com.huertaonline.app.databinding.FragmentMisPedidosBinding
import androidx.lifecycle.asLiveData

// Gestiona la visualización de todos los pedidos realizados por el usuario,
// actualizando la lista en tiempo real si el estado de alguno de ellos cambia.
class MisPedidosFragment : Fragment() {

    private var _binding: FragmentMisPedidosBinding? = null
    private val binding get() = _binding!!

    // Herramientas para acceder a los datos de pedidos y a la cuenta del usuario.
    private val pedidoRepo = PedidoRepository()
    private val authRepo   = AuthRepository()

    // El encargado de dibujar cada tarjeta de pedido en la lista.
    private lateinit var adapter: PedidoAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentMisPedidosBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Configuramos la lista visual para que los pedidos aparezcan uno debajo de otro.
        adapter = PedidoAdapter()
        binding.recyclerPedidos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MisPedidosFragment.adapter
        }

        // Recuperamos el código del usuario actual. Si no hay nadie conectado, no hace nada.
        val uid = authRepo.uidActual() ?: return

        // Conectamos con el almacén de pedidos en la nube:
        // Buscamos solo los pedidos del usuario y los observamos.
        pedidoRepo.obtenerDeConsumidor(uid).asLiveData()
            .observe(viewLifecycleOwner) { lista ->
                // Actualizamos la lista visual con los pedidos encontrados.
                adapter.actualizar(lista)

                // Si el usuario nunca ha comprado nada, mostramos un aviso de "Lista vacía".
                binding.tvVacio.visibility =
                    if (lista.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    // Limpieza de la memoria al cerrar la pantalla de historial.
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}