package com.huertaonline.app.ui.pedidos

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.huertaonline.app.data.repository.AuthRepository
import com.huertaonline.app.data.repository.PedidoRepository
import com.huertaonline.app.data.repository.ValoracionRepository
import com.huertaonline.app.databinding.FragmentMisPedidosBinding
import com.huertaonline.app.databinding.DialogValorarPedidoBinding
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// Gestiona la visualización de todos los pedidos realizados por el usuario,
// actualizando la lista en tiempo real si el estado de alguno de ellos cambia.
class MisPedidosFragment : Fragment() {

    private var _binding: FragmentMisPedidosBinding? = null
    private val binding get() = _binding!!

    // Herramientas para acceder a los datos de pedidos y a la cuenta del usuario.
    private val pedidoRepo = PedidoRepository()
    private val authRepo   = AuthRepository()
    private val valoracionRepo = ValoracionRepository()

    // El encargado de dibujar cada tarjeta de pedido en la lista.
    private lateinit var adapter: PedidoAdapter

    // Flag para evitar que el popup de valoración salga varias veces por error
    private var popupMostrado = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentMisPedidosBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Establecemos el título de la pantalla
        binding.tvTitulo.text = "Mis pedidos"

        // Configuramos la lista visual para que los pedidos aparezcan uno debajo de otro.
        adapter = PedidoAdapter { pedido, nuevoEstado ->
            actualizarPedido(pedido, nuevoEstado)
        }
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

    private fun actualizarPedido(pedido: com.huertaonline.app.data.model.Pedido, estado: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val res = pedidoRepo.actualizarEstado(pedido.id, estado)
            if (res.isSuccess) {
                if (estado == "entregado" && !popupMostrado) {
                    popupMostrado = true
                    mostrarPopupValoracion(pedido)
                }
            } else {
                Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
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
                popupMostrado = false // Reiniciamos para futuros pedidos
            }
            .setNegativeButton("Ahora no") { _, _ ->
                popupMostrado = false
            }
            .show()
    }

    private fun guardarValoraciones(pedido: com.huertaonline.app.data.model.Pedido, nota: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val uid = authRepo.uidActual() ?: return@launch
            val perfil = authRepo.obtenerPerfil(uid)
            val nombre = perfil?.nombre ?: "Usuario"
            
            // Valoramos cada producto del pedido
            pedido.items.forEach { item ->
                valoracionRepo.valorar(item.productoId, uid, nombre, nota)
            }
            Toast.makeText(requireContext(), "¡Gracias por tu valoración!", Toast.LENGTH_SHORT).show()
        }
    }

    // Limpieza de la memoria al cerrar la pantalla de historial.
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}