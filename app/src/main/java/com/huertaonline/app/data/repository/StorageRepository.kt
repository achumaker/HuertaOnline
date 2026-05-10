package com.huertaonline.app.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

// Se comunica con el servidor de archivos (Firebase Storage) para subir, organizar y
// borrar las imágenes que se muestran en la aplicación.
class StorageRepository {

    // Referencia al espacio de almacenamiento principal en la nube.
    private val ref = FirebaseStorage.getInstance().reference

    // Sube la foto de un producto. Organiza las imágenes por carpetas según
    // el identificador del productor y les asigna un nombre único al azar
    // para que ninguna foto se sobrescriba. Al terminar, devuelve el enlace público.
    suspend fun subirImagenProducto(uri: Uri, productorId: String): Result<String> {
        return try {
            val child = ref.child("productos/$productorId/${UUID.randomUUID()}.jpg")
            child.putFile(uri).await()
            Result.success(child.downloadUrl.await().toString())
        } catch (e: Exception) { Result.failure(e) }
    }

    // Sube o actualiza la foto de perfil de un usuario. En este caso, el archivo
    // siempre se llama "foto.jpg" dentro de la carpeta del usuario, así que
    // si sube una nueva, se reemplaza la anterior automáticamente.
    suspend fun subirFotoPerfil(uri: Uri, uid: String): Result<String> {
        return try {
            val child = ref.child("perfiles/$uid/foto.jpg")
            child.putFile(uri).await()
            Result.success(child.downloadUrl.await().toString())
        } catch (e: Exception) { Result.failure(e) }
    }

    // Borra una imagen del servidor utilizando su dirección de internet.
    // Si hay un error (por ejemplo, si la foto ya no existe), el sistema
    // continúa sin interrumpir el funcionamiento de la aplicación.
    suspend fun eliminarImagen(url: String) {
        try {
            FirebaseStorage.getInstance().getReferenceFromUrl(url).delete().await()
        } catch (_: Exception) { /* operación no crítica para el flujo principal */ }
    }
}