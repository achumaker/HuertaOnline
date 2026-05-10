package com.huertaonline.app.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.huertaonline.app.data.repository.AuthRepository
import com.huertaonline.app.databinding.FragmentPerfilBinding
import com.huertaonline.app.ui.auth.LoginActivity
import kotlinx.coroutines.launch

// Gestiona la visualización de los datos del cliente o productor y permite
// editar información como el teléfono o la dirección de entrega.
class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private val authRepo = AuthRepository()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentPerfilBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val uid = authRepo.uidActual() ?: return

        // ── Carga de datos ──
        // Recupera la información del usuario desde la nube para mostrarla en pantalla.
        viewLifecycleOwner.lifecycleScope.launch {
            val usuario = authRepo.obtenerPerfil(uid) ?: return@launch
            binding.apply {
                tvNombre.text    = usuario.nombre
                tvEmail.text     = usuario.email
                // Formatea el rol para que luzca profesional (ej: "Productor").
                tvRol.text       = usuario.rol.replaceFirstChar { it.uppercase() }
                tvDireccion.text = usuario.direccion.ifEmpty { "Sin dirección guardada" }

                // Si el usuario es un agricultor, muestra también el nombre de su huerta.
                if (usuario.rol == "productor") {
                    tvHuerta.visibility = View.VISIBLE
                    tvHuerta.text = "🌿 ${usuario.nombreHuerta}"
                }

                // Carga la foto de perfil en formato circular.
                Glide.with(requireContext())
                    .load(usuario.fotoUrl)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .circleCrop()
                    .into(ivAvatar)

                // Prepara los cuadros de texto con los datos actuales para poder editarlos.
                etTelefono.setText(usuario.telefono)
                etDireccion.setText(usuario.direccion)
            }
        }

        // ── Actualización de perfil ──
        // Recoge los nuevos datos introducidos y los guarda de forma permanente en la nube.
        binding.btnGuardarPerfil.setOnClickListener {
            val telefono  = binding.etTelefono.text.toString().trim()
            val direccion = binding.etDireccion.text.toString().trim()
            viewLifecycleOwner.lifecycleScope.launch {
                authRepo.actualizarPerfil(uid, mapOf(
                    "telefono"  to telefono,
                    "direccion" to direccion
                ))
                Toast.makeText(requireContext(),
                    "Perfil actualizado ✓", Toast.LENGTH_SHORT).show()
            }
        }

        // ── Salida segura ──
        // Gestiona el cierre de sesión tras pedir confirmación al usuario.
        binding.btnCerrarSesion.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que quieres salir?")
                .setPositiveButton("Salir") { _, _ ->
                    // Cierra la conexión y redirige a la pantalla de Login.
                    FirebaseAuth.getInstance().signOut()
                    startActivity(
                        Intent(requireContext(), LoginActivity::class.java).apply {
                            // Borra el historial de navegación para que no se pueda volver atrás.
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    // Libera la memoria de los elementos visuales al salir de la pantalla.
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}