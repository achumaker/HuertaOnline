package com.huertaonline.app.ui.carrito

import android.app.Application
import androidx.lifecycle.*
import com.huertaonline.app.data.local.AppDatabase
import com.huertaonline.app.data.local.CarritoItem
import com.huertaonline.app.data.model.Producto
import kotlinx.coroutines.launch

// Centraliza todas las operaciones de cálculo y modificación de
// los productos seleccionados por el cliente.
class CarritoViewModel(app: Application) : AndroidViewModel(app) {

    // Conexión con la base de datos local para realizar consultas y cambios.
    private val dao = AppDatabase.getInstance(app).carritoDao()

    // Lista de productos en el carrito. Se actualiza automáticamente
    // cada vez que hay un cambio en la base de datos.
    val items: LiveData<List<CarritoItem>> = dao.obtenerTodos().asLiveData()

    // Cálculo automático del precio total. Suma el resultado de multiplicar
    // el precio por la cantidad de cada artículo en la lista.
    val total: LiveData<Double> = items.map { lista ->
        lista.sumOf { it.precio * it.cantidad }
    }

    // Contador del número de artículos diferentes que hay en la cesta.
    val numItems: LiveData<Int> = dao.contarItems().asLiveData()

    // Función para añadir un producto al carrito con una cantidad específica.
    fun agregar(producto: Producto, cantidad: Int = 1) = viewModelScope.launch {
        // Verificamos si hay stock suficiente antes de añadir nada
        if (producto.stock < cantidad) return@launch

        val existente = items.value?.find { it.productoId == producto.id }

        if (existente != null) {
            // Verificamos que la suma no supere el stock total
            val nuevaCantidad = existente.cantidad + cantidad
            if (nuevaCantidad <= producto.stock) {
                dao.actualizarCantidad(producto.id, nuevaCantidad)
            }
        } else {
            // Si es nuevo, crea una ficha nueva con la cantidad indicada.
            dao.insertar(CarritoItem(
                productoId  = producto.id,
                nombre      = producto.nombre,
                precio      = producto.precio,
                cantidad    = cantidad,
                productorId = producto.productorId,
                imagenUrl   = producto.imagenUrl
            ))
        }
    }

    // Permite cambiar el número de unidades de un producto que ya está en el carrito.
    fun actualizarCantidad(item: CarritoItem, nuevaCantidad: Int) = viewModelScope.launch {
        if (nuevaCantidad > 0) {
            dao.actualizarCantidad(item.productoId, nuevaCantidad)
        }
    }

    // Quita un producto específico de la cesta.
    fun eliminar(item: CarritoItem) = viewModelScope.launch { dao.eliminar(item) }

    // Borra todos los productos y deja el carrito totalmente vacío.
    fun vaciar() = viewModelScope.launch { dao.vaciar() }
}