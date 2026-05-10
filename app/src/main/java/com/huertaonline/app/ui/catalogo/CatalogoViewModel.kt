package com.huertaonline.app.ui.catalogo

import androidx.lifecycle.*
import com.huertaonline.app.data.model.Producto
import com.huertaonline.app.data.repository.ProductoRepository
import kotlinx.coroutines.launch

// Procesa la lista de productos y aplicar los filtros de búsqueda sin necesidad de volver a pedirlos a internet,
// lo que hace que la aplicación sea mucho más rápida.
class CatalogoViewModel : ViewModel() {

    private val repo = ProductoRepository()

    // Esta es la lista "maestra" que viene de internet.
    // Se mantiene conectada en tiempo real: si un producto cambia en la nube,
    // esta lista se actualiza automáticamente aquí.
    val productos: LiveData<List<Producto>> = liveData {
        repo.obtenerTodos().collect { emit(it) }
    }

    // Esta es una lista secundaria que solo contiene los productos que
    // cumplen con los requisitos de búsqueda del usuario.
    private val _filtrados = MutableLiveData<List<Producto>>()
    val filtrados: LiveData<List<Producto>> = _filtrados

    // Función principal para buscar y organizar.
    fun filtrar(texto: String, categoria: String = "todas") {
        val base = productos.value ?: return // Si no hay productos cargados, no hace nada.

        _filtrados.value = base.filter { p ->
            // Comprueba si el texto escrito está en el nombre del producto
            // o en el nombre del agricultor que lo vende.
            val coincideTexto = texto.isEmpty() ||
                    p.nombre.contains(texto, ignoreCase = true) ||
                    p.productorNombre.contains(texto, ignoreCase = true)

            // Comprueba si el producto pertenece a la categoría seleccionada
            // o si el usuario quiere ver "todas".
            val coincideCategoria = categoria == "todas" ||
                    p.categoria.equals(categoria, ignoreCase = true)

            // Solo mantiene el producto si cumple ambas condiciones a la vez.
            coincideTexto && coincideCategoria
        }
    }

    // Ayuda a la pantalla a saber qué lista debe pintar: la completa
    // o la que tiene aplicados los filtros.
    fun listaActual() = if (_filtrados.value != null) filtrados else productos
}