package com.huertaonline.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Define qué información exacta vamos a guardar de cada producto que
// el usuario decida añadir a su compra.
@Entity(tableName = "carrito")
data class CarritoItem(
    // Este es el código único de cada producto. Sirve para identificarlo
    // y asegurar que no haya duplicados en la lista.
    @PrimaryKey
    val productoId: String,

    // El nombre comercial del artículo.
    val nombre: String,

    // El coste por unidad del producto.
    val precio: Double,

    // El número de unidades que el usuario quiere comprar.
    val cantidad: Int,

    // La dirección de internet donde está guardada la foto del producto
    // para poder mostrarla en la aplicación.
    val imagenUrl: String
)