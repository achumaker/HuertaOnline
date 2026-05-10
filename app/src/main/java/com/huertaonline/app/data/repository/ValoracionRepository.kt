package com.huertaonline.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.huertaonline.app.data.model.Valoracion
import kotlinx.coroutines.tasks.await

// Gestiona las opiniones de los clientes y se encarga de actualizar automáticamente
// la nota media de cada producto cuando alguien deja una nueva puntuación.
class ValoracionRepository {

    // Referencias a la base de datos principal y a la carpeta de "valoraciones".
    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("valoraciones")

    // Registra una nueva opinión y recalcula la nota media del producto afectado.
    suspend fun valorar(
        productoId: String, consumidorId: String,
        consumidorNombre: String, puntuacion: Int, comentario: String
    ) {
        // Creamos el objeto con la opinión del cliente.
        val valoracion = Valoracion(
            productoId = productoId, consumidorId = consumidorId,
            consumidorNombre = consumidorNombre, puntuacion = puntuacion,
            comentario = comentario
        )
        // Guardamos la valoración en la nube.
        col.add(valoracion).await()

        // ── Recalcular nota media ──
        // Buscamos todas las opiniones que existen para este producto concreto.
        val snap = col.whereEqualTo("productoId", productoId).get().await()

        // Extraemos las puntuaciones, calculamos el promedio y evitamos errores
        // en caso de que el resultado no sea un número válido.
        val media = snap.documents
            .mapNotNull { it.getLong("puntuacion")?.toDouble() }
            .average().takeIf { !it.isNaN() } ?: 0.0

        // Actualizamos la ficha del producto original con la nueva nota media calculada.
        db.collection("productos").document(productoId)
            .update("mediaValoracion", media).await()
    }

    // Recupera la lista de todos los comentarios y puntuaciones de un producto específico.
    suspend fun obtenerDeProducto(productoId: String): List<Valoracion> {
        val snap = col.whereEqualTo("productoId", productoId).get().await()
        return snap.documents.mapNotNull {
            it.toObject(Valoracion::class.java)?.copy(id = it.id)
        }
    }
}