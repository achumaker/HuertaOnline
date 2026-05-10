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

    // Función para añadir un producto al carrito.
    fun agregar(producto: Producto) = viewModelScope.launch {
        // Primero comprueba si el producto ya estaba en la cesta.
        val existente = items.value?.find { it.productoId == producto.id }

        if (existente != null) {
            // Si ya existía, simplemente le suma uno a la cantidad actual.
            dao.actualizarCantidad(producto.id, existente.cantidad + 1)
        } else {
            // Si es nuevo, crea una ficha nueva con cantidad inicial de 1.
            dao.insertar(CarritoItem(
                productoId  = producto.id,
                nombre      = producto.nombre,
                precio      = producto.precio,
                cantidad    = 1,
                productorId = producto.productorId,
                imagenUrl   = producto.imagenUrl
            ))
        }
    }

    // Quita un producto específico de la cesta.
    fun eliminar(item: CarritoItem) = viewModelScope.launch { dao.eliminar(item) }

    // Borra todos los productos y deja el carrito totalmente vacío.
    fun vaciar() = viewModelScope.launch { dao.vaciar() }
}