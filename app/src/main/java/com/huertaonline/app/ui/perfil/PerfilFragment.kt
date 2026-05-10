package com.huertaonline.app.ui.perfil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.huertaonline.app.data.model.Usuario
import com.huertaonline.app.data.repository.AuthRepository
import com.huertaonline.app.data.repository.StorageRepository
import com.huertaonline.app.databinding.FragmentPerfilBinding
import com.huertaonline.app.ui.auth.LoginActivity
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private val authRepo = AuthRepository()
    private val storageRepo = StorageRepository()
    private var snapshotListener: ListenerRegistration? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { subirFoto(it) }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentPerfilBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val uid = authRepo.uidActual() ?: return

        // Escuchar datos del usuario en tiempo real (reputación, foto, etc.)
        snapshotListener = FirebaseFirestore.getInstance()
            .collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, _ ->
                val usuario = snapshot?.toObject(Usuario::class.java) ?: return@addSnapshotListener
                binding.apply {
                    tvNombre.text    = usuario.nombre
                    tvEmail.text     = usuario.email
                    tvRol.text       = usuario.rol.replaceFirstChar { it.uppercase() }
                    tvDireccion.text = usuario.direccion.ifEmpty { "Sin dirección guardada" }

                    if (usuario.rol == "productor") {
                        tvHuerta.visibility = View.VISIBLE
                        tvHuerta.text = "🌿 ${usuario.nombreHuerta}"
                        
                        layoutReputacion.visibility = View.VISIBLE
                        val nota = usuario.reputacion
                        rbPerfil.rating = nota.toFloat()
                        tvReputacion.text = if (nota > 0) "%.1f".format(nota) else "Nuevo"
                    }

                    Glide.with(requireContext())
                        .load(usuario.fotoUrl)
                        .placeholder(android.R.drawable.ic_menu_myplaces)
                        .circleCrop()
                        .into(ivAvatar)

                    etTelefono.setText(usuario.telefono)
                    etDireccion.setText(usuario.direccion)
                    ivAvatar.setOnClickListener { imagePicker.launch("image/*") }
                }
            }

        binding.btnGuardarPerfil.setOnClickListener {
            val telefono  = binding.etTelefono.text.toString().trim()
            val direccion = binding.etDireccion.text.toString().trim()
            viewLifecycleOwner.lifecycleScope.launch {
                authRepo.actualizarPerfil(uid, mapOf("telefono" to telefono, "direccion" to direccion))
                Toast.makeText(requireContext(), "Perfil actualizado ✓", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCerrarSesion.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que quieres salir?")
                .setPositiveButton("Salir") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun subirFoto(uri: Uri) {
        val uid = authRepo.uidActual() ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            storageRepo.subirFotoPerfil(uri, uid).onSuccess { url ->
                authRepo.actualizarPerfil(uid, mapOf("fotoUrl" to url))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        _binding = null
    }
}
