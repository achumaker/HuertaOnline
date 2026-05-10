package com.huertaonline.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.huertaonline.app.data.model.Valoracion
import kotlinx.coroutines.tasks.await

// Gestiona las opiniones de los clientes y se encarga de actualizar automáticamente
// la nota media de cada producto y la reputación del productor.
class ValoracionRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("valoraciones")

    // Registra una nueva opinión y recalcula las notas medias.
    suspend fun valorar(
        productoId: String, consumidorId: String,
        consumidorNombre: String, puntuacion: Int, comentario: String = ""
    ): Result<Unit> {
        return try {
            // 1. Obtener datos del producto para saber quién es el productor
            val productoDoc = db.collection("productos").document(productoId).get().await()
            val productorId = productoDoc.getString("productorId") ?: ""

            // 2. Crear y guardar la valoración con el ID del productor incluido
            val valoracion = Valoracion(
                productoId = productoId, 
                productorId = productorId,
                consumidorId = consumidorId,
                consumidorNombre = consumidorNombre, 
                puntuacion = puntuacion,
                comentario = comentario
            )
            col.add(valoracion).await()

            // 3. Recalcular nota media del PRODUCTO
            val snapProds = col.whereEqualTo("productoId", productoId).get().await()
            val mediaProd = snapProds.documents
                .mapNotNull { it.get("puntuacion")?.toString()?.toDoubleOrNull() }
                .average().takeIf { !it.isNaN() } ?: 0.0

            db.collection("productos").document(productoId)
                .update("mediaValoracion", mediaProd).await()

            // 4. Recalcular nota media del PRODUCTOR (Reputación global)
            // Consultamos TODAS las valoraciones de TODOS los productos de este productor
            if (productorId.isNotEmpty()) {
                val snapValoracionesProductor = col.whereEqualTo("productorId", productorId).get().await()
                
                val todasLasNotas = snapValoracionesProductor.documents
                    .mapNotNull { it.get("puntuacion")?.toString()?.toDoubleOrNull() }

                val mediaGlobal = if (todasLasNotas.isNotEmpty()) todasLasNotas.average() else 0.0

                db.collection("usuarios").document(productorId)
                    .update("reputacion", mediaGlobal).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Recupera la lista de todos los comentarios y puntuaciones de un producto específico.
    suspend fun obtenerDeProducto(productoId: String): List<Valoracion> {
        val snap = col.whereEqualTo("productoId", productoId).get().await()
        return snap.documents.mapNotNull {
            it.toObject(Valoracion::class.java)?.copy(id = it.id)
        }
    }
}
