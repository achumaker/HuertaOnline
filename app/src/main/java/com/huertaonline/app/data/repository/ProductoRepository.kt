package com.huertaonline.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.huertaonline.app.data.model.Producto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Se encarga de mostrar los artículos disponibles, organizarlos por categorías y
// permitir que los productores gestionen su oferta.
class ProductoRepository {

    // Referencia a la colección de "productos" en la base de datos en la nube.
    private val col = FirebaseFirestore.getInstance().collection("productos")

    // Muestra todos los productos que están marcados como "activos".
    // Los ordena alfabéticamente y actualiza la lista automáticamente
    // si hay cambios (como un cambio de precio o de stock).
    fun obtenerTodos(): Flow<List<Producto>> = callbackFlow {
        val listener = col
            .whereEqualTo("activo", true)
            .orderBy("nombre", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }

                // Convierte los datos de la nube en objetos que la app pueda mostrar.
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(Producto::class.java)?.copy(id = it.id)
                } ?: emptyList())
            }
        // Desconecta la escucha cuando el usuario sale de la pantalla para no consumir recursos.
        awaitClose { listener.remove() }
    }

    // Recupera únicamente los productos que pertenecen a un productor específico.
    // Útil para que cada agricultor gestione solo su propio inventario.
    fun obtenerDeProductor(uid: String): Flow<List<Producto>> = callbackFlow {
        val listener = col
            .whereEqualTo("productorId", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(Producto::class.java)?.copy(id = it.id)
                } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    // Filtra la base de datos para mostrar solo productos de una sección
    // concreta (ej: "frutas" o "verduras").
    suspend fun buscarPorCategoria(categoria: String): List<Producto> {
        val snap = col
            .whereEqualTo("activo", true)
            .whereEqualTo("categoria", categoria.lowercase())
            .get().await()
        return snap.documents.mapNotNull {
            it.toObject(Producto::class.java)?.copy(id = it.id)
        }
    }

    // Registra un nuevo producto en el sistema.
    // Devuelve el código identificador generado para el nuevo artículo.
    suspend fun crear(producto: Producto): Result<String> {
        return try {
            val ref = col.add(producto).await()
            Result.success(ref.id)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Permite modificar datos sueltos de un producto, como el precio o la descripción.
    suspend fun actualizar(id: String, datos: Map<String, Any>) =
        col.document(id).update(datos).await()

    // En lugar de borrar el producto del historial, simplemente lo marca como "no activo".
    // Así, el producto deja de aparecer en la tienda pero se mantiene el registro
    // para pedidos antiguos.
    suspend fun desactivar(id: String) =
        col.document(id).update("activo", false).await()
}