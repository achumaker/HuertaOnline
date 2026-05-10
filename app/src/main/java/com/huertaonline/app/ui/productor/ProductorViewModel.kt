package com.huertaonline.app.ui.productor

import android.net.Uri
import androidx.lifecycle.*
import com.huertaonline.app.data.model.Producto
import com.huertaonline.app.data.repository.*
import kotlinx.coroutines.launch

// Maneja el ciclo de vida de los productos desde la perspectiva del vendedor,
// incluyendo la subida de archivos multimedia y la actualización del catálogo personal.
class ProductorViewModel : ViewModel() {

    private val authRepo    = AuthRepository()
    private val productoRepo = ProductoRepository()
    private val storageRepo  = StorageRepository()

    // Representa los estados del proceso de guardado para informar a la interfaz.
    sealed class Estado {
        object    Inactivo                   : Estado()
        object    Cargando                   : Estado()
        object    Exito                      : Estado()
        data class Error(val msg: String)     : Estado()
    }

    val estadoGuardado = MutableLiveData<Estado>(Estado.Inactivo)

    // Lista de productos que pertenecen exclusivamente al productor actual.
    // Se actualiza en tiempo real: si el stock cambia, el productor lo ve al instante.
    val misProductos: LiveData<List<Producto>> = liveData {
        val uid = authRepo.uidActual() ?: return@liveData
        productoRepo.obtenerDeProductor(uid).collect { emit(it) }
    }

    // Proceso complejo de creación: sube la imagen primero y luego guarda los datos.
    fun crearProducto(
        nombre: String, descripcion: String, precio: Double,
        stock: Int, unidad: String, categoria: String,
        imagenUri: Uri?
    ) {
        estadoGuardado.value = Estado.Cargando
        viewModelScope.launch {
            // 1. Verificación de identidad
            val uid = authRepo.uidActual() ?: run {
                estadoGuardado.value = Estado.Error("No autenticado")
                return@launch
            }
            val perfil = authRepo.obtenerPerfil(uid)

            // 2. Gestión de la imagen
            var imagenUrl = ""
            if (imagenUri != null) {
                val r = storageRepo.subirImagenProducto(imagenUri, uid)
                if (r.isFailure) {
                    estadoGuardado.value = Estado.Error("Error subiendo imagen")
                    return@launch
                }
                imagenUrl = r.getOrThrow()
            }

            // 3. Creación del objeto Producto con los datos finales
            val producto = Producto(
                nombre = nombre, descripcion = descripcion, precio = precio,
                stock = stock, unidad = unidad, categoria = categoria.lowercase(),
                productorId = uid,
                // Si el productor tiene nombre de huerta, lo usa; si no, usa su nombre personal.
                productorNombre = perfil?.nombreHuerta?.ifEmpty { perfil.nombre } ?: "",
                imagenUrl = imagenUrl
            )

            // 4. Guardado en la base de datos
            val r = productoRepo.crear(producto)
            estadoGuardado.value = if (r.isSuccess) Estado.Exito
            else Estado.Error("Error guardando producto")
        }
    }

    // Elimina o deshabilita un producto del catálogo.
    fun desactivarProducto(id: String) = viewModelScope.launch {
        productoRepo.desactivar(id)
    }
}