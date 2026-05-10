package com.huertaonline.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Contiene la lista de acciones permitidas que podemos realizar sobre
// la información guardada en el carrito.
@Dao
interface CarritoDao {

    // Recupera la lista completa de productos guardados en el carrito.
    // Se mantiene actualizada automáticamente si los datos cambian.
    @Query("SELECT * FROM carrito")
    fun obtenerTodos(): Flow<List<CarritoItem>>

    // Guarda un nuevo producto. Si el producto ya existía en la lista,
    // actualiza su información con la nueva.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(item: CarritoItem)

    // Borra un producto específico de la lista del carrito.
    @Delete
    suspend fun eliminar(item: CarritoItem)

    // Elimina absolutamente todos los productos guardados, dejando el carrito a cero.
    @Query("DELETE FROM carrito")
    suspend fun vaciar()

    // Devuelve el número total de productos que hay actualmente en el carrito.
    @Query("SELECT COUNT(*) FROM carrito")
    fun contarItems(): Flow<Int>

    // Modifica únicamente el número de unidades de un producto concreto
    // usando su identificador único.
    @Query("UPDATE carrito SET cantidad = :cantidad WHERE productoId = :id")
    suspend fun actualizarCantidad(id: String, cantidad: Int)
}