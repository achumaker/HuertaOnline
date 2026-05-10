package com.huertaonline.app.ui.productor

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.huertaonline.app.data.repository.AuthRepository
import com.huertaonline.app.data.repository.PedidoRepository
import com.huertaonline.app.databinding.FragmentMisPedidosBinding
import kotlinx.coroutines.launch

// Pantalla para que el productor vea los pedidos que han hecho de sus productos.
class PedidosProductorFragment : Fragment() {

    private var _binding: FragmentMisPedidosBinding? = null
    private val binding get() = _binding!!

    private val pedidoRepo = PedidoRepository()
    private val authRepo   = AuthRepository()

    private lateinit var adapter: PedidoProductorAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentMisPedidosBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val uid = authRepo.uidActual() ?: return

        adapter = PedidoProductorAdapter(uid) { pedido, nuevoEstado ->
            viewLifecycleOwner.lifecycleScope.launch {
                val resultado = pedidoRepo.actualizarEstado(pedido.id, nuevoEstado)
                if (resultado.isSuccess) {
                    Toast.makeText(requireContext(), "¡Pedido marcado como ${nuevoEstado.uppercase()}!", Toast.LENGTH_SHORT).show()
                } else {
                    val error = resultado.exceptionOrNull()?.message ?: "Error desconocido"
                    Toast.makeText(requireContext(), "Fallo: $error", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.apply {
            recyclerPedidos.layoutManager = LinearLayoutManager(requireContext())
            recyclerPedidos.adapter = adapter
        }

        pedidoRepo.obtenerDeProductor(uid).asLiveData()
            .observe(viewLifecycleOwner) { lista ->
                adapter.actualizar(lista)
                binding.tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                if (lista.isEmpty()) {
                    binding.tvVacio.text = "Aún no has recibido ventas."
                }
            }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
