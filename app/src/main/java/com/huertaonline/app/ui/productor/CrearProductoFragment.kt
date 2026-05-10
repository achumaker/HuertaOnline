package com.huertaonline.app.ui.productor

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.huertaonline.app.databinding.FragmentCrearProductoBinding

// Gestiona el formulario de alta para nuevos artículos, controlando
// la selección de imágenes y la validación de los datos antes de subirlos a la nube.
class CrearProductoFragment : Fragment() {

    private var _binding: FragmentCrearProductoBinding? = null
    private val binding get() = _binding!!

    // Motor que gestiona la lógica del productor y el estado de la subida.
    private val vm: ProductorViewModel by viewModels()

    // Variable temporal para guardar la ubicación de la foto elegida en el móvil.
    private var imagenUri: Uri? = null

    // Herramienta para abrir la galería del teléfono y seleccionar una imagen.
    private val seleccionarImagen = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imagenUri = it
            // Muestra una vista previa de la foto seleccionada en la pantalla.
            Glide.with(this).load(it).centerCrop().into(binding.ivPreview)
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentCrearProductoBinding.inflate(i, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // ── Configuración de listas desplegables ──
        // Carga las opciones de categorías (fruta, verdura...) y unidades (kg, docena...)
        // desde los archivos de configuración de la app.
        val categorias = resources.getStringArray(com.huertaonline.app.R.array.categorias)
        val unidades   = resources.getStringArray(com.huertaonline.app.R.array.unidades)

        binding.spinnerCategoria.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerUnidad.adapter    = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unidades).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Botón para abrir la galería y elegir la foto del producto.
        binding.btnSeleccionarImagen.setOnClickListener { seleccionarImagen.launch("image/*") }

        // ── Acción de Guardar ──
        binding.btnGuardar.setOnClickListener {
            val nombre      = binding.etNombre.text.toString().trim()
            val descripcion = binding.etDescripcion.text.toString().trim()
            val precioStr   = binding.etPrecio.text.toString()
            val stockStr    = binding.etStock.text.toString()

            // Validación: No permite guardar si faltan los datos básicos.
            if (nombre.isEmpty() || precioStr.isEmpty()) {
                Toast.makeText(requireContext(), "Nombre y precio son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Envía toda la información al motor (ViewModel) para que procese el alta.
            vm.crearProducto(
                nombre      = nombre,
                descripcion = descripcion,
                precio      = precioStr.toDoubleOrNull() ?: 0.0,
                stock       = stockStr.toIntOrNull() ?: 0,
                unidad      = binding.spinnerUnidad.selectedItem.toString(),
                categoria   = binding.spinnerCategoria.selectedItem.toString(),
                imagenUri   = imagenUri
            )
        }

        // ── Vigilancia del estado ──
        // Observa si el producto se está subiendo, si ha habido éxito o un error.
        vm.estadoGuardado.observe(viewLifecycleOwner) { estado ->
            // Muestra la rueda de carga mientras se suben los datos y la foto.
            binding.progressBar.visibility =
                if (estado is ProductorViewModel.Estado.Cargando) View.VISIBLE else View.GONE

            when (estado) {
                is ProductorViewModel.Estado.Exito -> {
                    // Si todo sale bien, avisa y vuelve automáticamente a la pantalla anterior.
                    Toast.makeText(requireContext(), "Producto creado ✅", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is ProductorViewModel.Estado.Error ->
                    // Si falla, muestra el motivo del error.
                    Toast.makeText(requireContext(), estado.msg, Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}