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
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class PedidosProductorFragment : Fragment() {

    private var _binding: FragmentMisPedidosBinding? = null
    private val binding get() = _binding!!

    private val pedidoRepo = PedidoRepository()
    private val authRepo   = AuthRepository()

    private lateinit var adapter: PedidoProductorAdapter
    private var listaCompleta: List<com.huertaonline.app.data.model.Pedido> = emptyList()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentMisPedidosBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTitulo.text = "Ventas recibidas"

        configurarTabs()

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
                listaCompleta = lista
                filtrarPedidos(binding.tabLayout.selectedTabPosition)
            }
    }

    private fun configurarTabs() {
        binding.tabLayout.apply {
            addTab(newTab().setText("Pendientes"))
            addTab(newTab().setText("Enviados"))
            addTab(newTab().setText("Entregados"))

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    filtrarPedidos(tab?.position ?: 0)
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun filtrarPedidos(posicion: Int) {
        val estadoFiltrar = when (posicion) {
            0 -> "pendiente"
            1 -> "enviado"
            2 -> "entregado"
            else -> "pendiente"
        }

        val listaFiltrada = listaCompleta.filter { 
            if (estadoFiltrar == "pendiente") {
                it.estado == "pendiente" || it.estado == "preparando"
            } else {
                it.estado == estadoFiltrar
            }
        }
        
        adapter.actualizar(listaFiltrada)
        binding.tvVacio.visibility = if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
