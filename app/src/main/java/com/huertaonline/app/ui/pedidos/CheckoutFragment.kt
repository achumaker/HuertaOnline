package com.huertaonline.app.ui.pedidos

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.huertaonline.app.R
import com.huertaonline.app.data.model.ItemPedido
import com.huertaonline.app.data.model.Pedido
import com.huertaonline.app.data.repository.AuthRepository
import com.huertaonline.app.data.repository.PedidoRepository
import com.huertaonline.app.databinding.FragmentCheckoutBinding
import com.huertaonline.app.ui.carrito.CarritoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Gestiona el último paso: muestra el resumen de la cuenta, solicita
// la dirección y confirma la transacción.
class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    // Acceso a los datos del carrito, la base de datos de pedidos y la cuenta del usuario.
    private val carritoVm: CarritoViewModel by activityViewModels()
    private val pedidoRepo = PedidoRepository()
    private val authRepo   = AuthRepository()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentCheckoutBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Muestra el importe final que el usuario debe abonar.
        carritoVm.total.observe(viewLifecycleOwner) {
            binding.tvTotal.text = "Total a pagar: ${"%.2f".format(it)}€"
        }

        // Genera un resumen visual de todos los productos, cantidades y subtotales.
        carritoVm.items.observe(viewLifecycleOwner) { items ->
            binding.tvResumen.text = items.joinToString("\n") {
                "• ${it.nombre} x${it.cantidad} — ${"%.2f".format(it.precio * it.cantidad)}€"
            }
        }

        // Al pulsar "Pagar", verifica que se haya escrito una dirección antes de continuar.
        binding.btnPagar.setOnClickListener {
            val dir = binding.etDireccion.text.toString().trim()
            if (dir.isEmpty()) {
                Toast.makeText(requireContext(), "Indica la dirección de entrega", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mostrarDialogoPago(dir)
        }
    }

    // Muestra una ventana emergente para que el usuario introduzca sus datos bancarios.
    private fun mostrarDialogoPago(direccion: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pago, null)
        AlertDialog.Builder(requireContext())
            .setTitle("💳 Datos de pago")
            .setView(dialogView)
            .setPositiveButton("Pagar") { _, _ ->
                // Bloquea el botón y muestra una rueda de carga para evitar clics dobles.
                binding.progressPago.visibility = View.VISIBLE
                binding.btnPagar.isEnabled = false

                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1500) // Simula el tiempo que tarda un banco en validar la tarjeta.
                    crearPedido(direccion)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Una vez "pagado", recopila toda la información y crea el registro oficial del pedido.
    private suspend fun crearPedido(direccion: String) {
        val uid     = authRepo.uidActual() ?: return
        val perfil  = authRepo.obtenerPerfil(uid)
        val items   = carritoVm.items.value ?: emptyList()
        val total   = carritoVm.total.value ?: 0.0

        // Crea el objeto con todos los datos: quién compra, qué compra y dónde se envía.
        val pedido = Pedido(
            consumidorId     = uid,
            consumidorNombre = perfil?.nombre ?: "",
            items            = items.map { ItemPedido(it.productoId, it.nombre, it.cantidad, it.precio) },
            total            = total,
            direccionEnvio   = direccion,
            estado           = "pendiente"
        )

        // Guarda el pedido en la base de datos de internet.
        val resultado = pedidoRepo.crear(pedido)
        binding.progressPago.visibility = View.GONE
        binding.btnPagar.isEnabled = true

        if (resultado.isSuccess) {
            // Si todo ha ido bien, vacía el carrito local y lleva al usuario a su historial.
            carritoVm.vaciar()
            Toast.makeText(requireContext(),
                "✅ ¡Pedido realizado con éxito!", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_checkout_to_pedidos)
        } else {
            // Si hubo un fallo de red, avisa al usuario para que lo intente de nuevo.
            Toast.makeText(requireContext(),
                "Error al crear el pedido. Intenta de nuevo.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}