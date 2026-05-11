package com.huertaonline.app.ui.pedidos

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.huertaonline.app.data.repository.AuthRepository
import com.huertaonline.app.data.repository.PedidoRepository
import com.huertaonline.app.data.repository.ValoracionRepository
import com.huertaonline.app.databinding.FragmentMisPedidosBinding
import com.huertaonline.app.databinding.DialogValorarPedidoBinding
import com.google.android.material.tabs.TabLayout
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MisPedidosFragment : Fragment() {

    private var _binding: FragmentMisPedidosBinding? = null
    private val binding get() = _binding!!

    private val pedidoRepo = PedidoRepository()
    private val authRepo   = AuthRepository()
    private val valoracionRepo = ValoracionRepository()

    private lateinit var adapter: PedidoAdapter
    private var procesandoId: String? = null 

    private var listaCompleta: List<com.huertaonline.app.data.model.Pedido> = emptyList()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentMisPedidosBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTitulo.text = "Mis pedidos"

        configurarTabs()

        adapter = PedidoAdapter { pedido, nuevoEstado ->
            if (procesandoId != pedido.id) {
                actualizarPedido(pedido, nuevoEstado)
            }
        }
        binding.recyclerPedidos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MisPedidosFragment.adapter
        }

        val uid = authRepo.uidActual() ?: return
        pedidoRepo.obtenerDeConsumidor(uid).asLiveData()
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
            // Consideramos "preparando" como parte de "pendientes" visualmente para simplificar
            if (estadoFiltrar == "pendiente") {
                it.estado == "pendiente" || it.estado == "preparando"
            } else {
                it.estado == estadoFiltrar
            }
        }
        
        adapter.actualizar(listaFiltrada)
        binding.tvVacio.visibility = if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun actualizarPedido(pedido: com.huertaonline.app.data.model.Pedido, estado: String) {
        if (pedido.estado == estado) return // Ya está en ese estado
        
        viewLifecycleOwner.lifecycleScope.launch {
            procesandoId = pedido.id
            val res = pedidoRepo.actualizarEstado(pedido.id, estado)
            if (res.isSuccess) {
                if (estado == "entregado") {
                    mostrarPopupValoracion(pedido)
                }
            } else {
                Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                procesandoId = null
            }
        }
    }

    private fun mostrarPopupValoracion(pedido: com.huertaonline.app.data.model.Pedido) {
        val b = DialogValorarPedidoBinding.inflate(layoutInflater)
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(b.root)
            .setCancelable(false)
            .setTitle("Valora tu pedido")
            .setPositiveButton("Valorar") { _, _ ->
                val nota = b.ratingBar.rating.toInt()
                guardarValoraciones(pedido, nota)
                procesandoId = null
            }
            .setNegativeButton("Ahora no") { _, _ ->
                procesandoId = null
            }
            .show()
    }

    private fun guardarValoraciones(pedido: com.huertaonline.app.data.model.Pedido, nota: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val uid = authRepo.uidActual() ?: return@launch
            val perfil = authRepo.obtenerPerfil(uid)
            val nombre = perfil?.nombre ?: "Usuario"
            
            // Pasamos el productorId directamente desde el item para evitar errores de búsqueda
            pedido.items.forEach { item ->
                valoracionRepo.valorar(
                    productoId = item.productoId,
                    consumidorId = uid,
                    consumidorNombre = nombre,
                    puntuacion = nota,
                    productorId = item.productorId // Nuevo parámetro directo
                )
            }
            Toast.makeText(requireContext(), "¡Gracias por tu valoración!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
