package com.huertaonline.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.huertaonline.app.data.model.Pedido
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Controla la comunicación con la base de datos en la nube para guardar nuevas compras
// y vigilar los cambios en el estado de los envíos.
class PedidoRepository {

    // Referencia directa a la carpeta de "pedidos" en la base de datos de la nube.
    private val col = FirebaseFirestore.getInstance().collection("pedidos")

    // Registra una nueva compra. Si tiene éxito, devuelve el código identificador
    // que el sistema le ha asignado automáticamente al pedido.
    suspend fun crear(pedido: Pedido): Result<String> {
        return try {
            val ref = col.add(pedido).await()
            Result.success(ref.id)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Busca y muestra todos los pedidos realizados por un usuario concreto.
    // Los organiza por fecha, mostrando primero los más recientes.
    // Gracias al "listener", si el estado del pedido cambia, la pantalla se actualiza sola.
    fun obtenerDeConsumidor(uid: String): Flow<List<Pedido>> = callbackFlow {
        val listener = col
            .whereEqualTo("consumidorId", uid)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                // Si ocurre un error en la conexión, se cierra el flujo de datos.
                if (err != null) { close(err); return@addSnapshotListener }

                // Transforma los datos recibidos de la nube en una lista de pedidos
                // que la aplicación pueda entender y mostrar.
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(Pedido::class.java)?.copy(id = it.id)
                } ?: emptyList())
            }
        // Si dejamos de mirar esta pantalla, se corta la conexión para ahorrar batería y datos.
        awaitClose { listener.remove() }
    }

    // Permite cambiar la situación de un pedido (por ejemplo, de "pendiente" a "enviado").
    suspend fun actualizarEstado(pedidoId: String, estado: String) =
        col.document(pedidoId).update("estado", estado).await()
}