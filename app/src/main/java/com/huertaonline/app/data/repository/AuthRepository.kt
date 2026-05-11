package com.huertaonline.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.huertaonline.app.data.model.Usuario
import kotlinx.coroutines.tasks.await

// Centraliza la comunicación con los servicios externos de seguridad (Firebase) para
// gestionar quién puede entrar en la aplicación y qué permisos tiene.
class AuthRepository {

    // Herramientas internas para gestionar el acceso (auth)
    // y la base de datos en la nube (db).
    private val auth     = FirebaseAuth.getInstance()
    private val db       = FirebaseFirestore.getInstance()
    private val usuarios = db.collection("usuarios")

    // ── Gestión de Sesión ──────────────────────────────────────────

    // Comprueba si hay alguien con la sesión abierta actualmente.
    fun hayUsuarioLogueado() = auth.currentUser != null

    // Devuelve el código de identificación único del usuario actual.
    fun uidActual() = auth.currentUser?.uid

    // Desconecta al usuario y cierra la sesión de forma segura.
    fun cerrarSesion() = auth.signOut()

    // Consulta en la base de datos si el usuario es "consumidor" o "productor".
    suspend fun obtenerRolActual(): String? {
        val uid = auth.currentUser?.uid ?: return null
        return usuarios.document(uid).get().await().getString("rol")
    }

    // ── Acceso con correo y contraseña ────────────────────────────────

    // Valida las credenciales. Si son correctas, busca el perfil completo del usuario.
    suspend fun loginEmail(email: String, password: String): Result<Usuario> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val usuario = obtenerPerfil(auth.currentUser!!.uid)
                ?: throw Exception("Perfil no encontrado")
            Result.success(usuario)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Registro de nuevos usuarios ───────────────────────────────────

    // Crea una cuenta nueva de seguridad y, acto seguido, guarda sus datos personales
    // y su rol en la base de datos de la aplicación.
    suspend fun registrarEmail(
        email: String, password: String,
        nombre: String, rol: String, nombreHuerta: String = ""
    ): Result<Usuario> {
        return try {
            val uid = auth.createUserWithEmailAndPassword(email, password)
                .await().user!!.uid
            val usuario = Usuario(
                uid = uid, nombre = nombre, email = email,
                rol = rol, nombreHuerta = nombreHuerta
            )
            usuarios.document(uid).set(usuario).await()
            Result.success(usuario)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Gestiona el acceso rápido. Si el usuario no tiene perfil, lanza un error para pedir el rol.
    suspend fun loginGoogle(idToken: String): Result<Usuario> {
        return try {
            val credencial = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credencial).await()
            val fireUser = auth.currentUser!!
            val doc = usuarios.document(fireUser.uid).get().await()
            
            if (!doc.exists()) {
                // Devolvemos un fallo controlado para que el ViewModel pida el rol
                throw Exception("perfil_inexistente")
            } else {
                Result.success(doc.toObject(Usuario::class.java)!!)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // Crea un perfil en la base de datos de forma manual (usado tras elegir rol en Google)
    suspend fun crearPerfilManual(usuario: Usuario): Result<Unit> {
        return try {
            usuarios.document(usuario.uid).set(usuario).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Gestión del Perfil ────────────────────────────────────────────

    // Obtiene toda la información de un usuario registrado mediante su código.
    suspend fun obtenerPerfil(uid: String): Usuario? =
        usuarios.document(uid).get().await().toObject(Usuario::class.java)

    // Permite modificar datos específicos del perfil (como el teléfono o la dirección).
    suspend fun actualizarPerfil(uid: String, datos: Map<String, Any>) =
        usuarios.document(uid).update(datos).await()
}