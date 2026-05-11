package com.huertaonline.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
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

    // Registra una nueva compra y descuenta el stock de los productos comprados.
    suspend fun crear(pedido: Pedido): Result<String> {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        
        // Creamos una nueva referencia para obtener un ID automático
        val pedidoRef = col.document()
        
        // Añadimos el pedido al lote (batch)
        batch.set(pedidoRef, pedido)
        
        // Por cada artículo en el pedido, generamos una orden de descuento de stock
        pedido.items.forEach { item ->
            val prodRef = db.collection("productos").document(item.productoId)
            // FieldValue.increment(-X) resta unidades de forma segura en el servidor
            batch.update(prodRef, "stock", FieldValue.increment(-item.cantidad.toLong()))
        }

        return try {
            batch.commit().await()
            Result.success(pedidoRef.id)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Busca y muestra todos los pedidos realizados por un usuario concreto.
    fun obtenerDeConsumidor(uid: String): Flow<List<Pedido>> = callbackFlow {
        val listener = col
            .whereEqualTo("consumidorId", uid)
            // Quitamos el orderBy para evitar errores de índices manuales en Firebase.
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }

                val lista = snap?.documents?.mapNotNull {
                    it.toObject(Pedido::class.java)?.copy(id = it.id)
                } ?: emptyList()

                // Ordenamos localmente para mayor fiabilidad.
                trySend(lista.sortedByDescending { it.fecha })
            }
        awaitClose { listener.remove() }
    }

    // Busca y muestra todos los pedidos que contienen productos de un productor concreto.
    fun obtenerDeProductor(productorUid: String): Flow<List<Pedido>> = callbackFlow {
        // Usamos una consulta específica para evitar errores de permisos (PERMISSION_DENIED).
        // Firestore solo permite leer si la consulta coincide con lo que el usuario tiene permitido ver.
        val listener = col
            .whereArrayContains("productorIds", productorUid)
            .addSnapshotListener { snap, err ->
                // Si hay un error (ej: falta de red), no borramos lo que ya se ve en pantalla.
                if (err != null) return@addSnapshotListener

                val lista = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Pedido::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                // Ordenamos por fecha descendente manualmente para no depender de índices de Firebase.
                trySend(lista.sortedByDescending { it.fecha })
            }
        awaitClose { listener.remove() }
    }

    // Permite cambiar la situación de un pedido (por ejemplo, de "pendiente" a "enviado").
    suspend fun actualizarEstado(pedidoId: String, estado: String): Result<Unit> {
        return try {
            if (pedidoId.isBlank()) throw Exception("ID de pedido no válido")
            col.document(pedidoId).update("estado", estado).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
