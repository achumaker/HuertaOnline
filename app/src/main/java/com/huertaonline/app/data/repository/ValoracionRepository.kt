package com.huertaonline.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.huertaonline.app.data.model.Valoracion
import kotlinx.coroutines.tasks.await

class ValoracionRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("valoraciones")

    suspend fun valorar(
        productoId: String, consumidorId: String,
        consumidorNombre: String, puntuacion: Int, 
        productorId: String = "", // ID del productor opcional para mayor precisión
        comentario: String = ""
    ): Result<Unit> {
        return try {
            // 1. Determinar el ID del productor
            val finalProductorId = if (productorId.isNotEmpty()) productorId else {
                val productoDoc = db.collection("productos").document(productoId).get().await()
                productoDoc.getString("productorId") ?: ""
            }

            // 2. Guardar la nueva valoración
            val valoracion = Valoracion(
                productoId = productoId, 
                productorId = finalProductorId,
                consumidorId = consumidorId,
                consumidorNombre = consumidorNombre, 
                puntuacion = puntuacion,
                comentario = comentario
            )
            col.add(valoracion).await()

            // 3. Recalcular y actualizar la media del PRODUCTO
            val snapProds = col.whereEqualTo("productoId", productoId).get().await()
            val notasProd = snapProds.documents.mapNotNull { 
                it.get("puntuacion")?.toString()?.toDoubleOrNull() 
            }
            val mediaProd = if (notasProd.isNotEmpty()) notasProd.average() else puntuacion.toDouble()
            
            db.collection("productos").document(productoId)
                .update("mediaValoracion", mediaProd).await()

            // 4. Recalcular y actualizar la reputación del PRODUCTOR
            if (finalProductorId.isNotEmpty()) {
                val snapTodas = col.whereEqualTo("productorId", finalProductorId).get().await()
                val notasTotales = snapTodas.documents.mapNotNull { 
                    it.get("puntuacion")?.toString()?.toDoubleOrNull() 
                }
                val mediaGlobal = if (notasTotales.isNotEmpty()) notasTotales.average() else puntuacion.toDouble()

                db.collection("usuarios").document(finalProductorId)
                    .update("reputacion", mediaGlobal).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) { 
            Log.e("STARS", "Error al valorar: ${e.message}")
            Result.failure(e) 
        }
    }

    suspend fun obtenerDeProducto(productoId: String): List<Valoracion> {
        val snap = col.whereEqualTo("productoId", productoId).get().await()
        return snap.documents.mapNotNull {
            it.toObject(Valoracion::class.java)?.copy(id = it.id)
        }
    }
}
