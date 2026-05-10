package com.huertaonline.app.ui.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huertaonline.app.data.model.Usuario
import com.huertaonline.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

// Organiza los intentos de acceso y traduce las respuestas técnicas en mensajes
// que el usuario pueda entender.
class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()

    // Definimos las cuatro situaciones posibles en las que puede estar la pantalla:
    // 1. Inactivo: No se está haciendo nada.
    // 2. Cargando: Se está validando la información.
    // 3. Exito: El usuario ha entrado correctamente.
    // 4. Error: Algo ha fallado y tenemos un mensaje explicativo.
    sealed class Estado {
        object    Inactivo                                      : Estado()
        object    Cargando                                      : Estado()
        data class Exito(val usuario: Usuario, val rol: String) : Estado()
        data class Error(val mensaje: String)                   : Estado()
    }

    // Variable que observa la pantalla para saber qué mostrar en cada momento.
    val estado = MutableLiveData<Estado>(Estado.Inactivo)

    // Al iniciar la app, verifica rápidamente si el usuario ya tenía la sesión abierta.
    fun comprobarSesion(callback: (String?) -> Unit) {
        if (!repo.hayUsuarioLogueado()) { callback(null); return }
        viewModelScope.launch { callback(repo.obtenerRolActual()) }
    }

    // Gestiona el intento de entrada con correo y clave.
    fun login(email: String, password: String) {
        estado.value = Estado.Cargando
        viewModelScope.launch {
            val r = repo.loginEmail(email, password)
            estado.value = if (r.isSuccess) {
                val u = r.getOrThrow()
                Estado.Exito(u, u.rol)
            } else {
                // Si falla, traduce el código técnico a un texto comprensible.
                Estado.Error(traducir(r.exceptionOrNull()?.message))
            }
        }
    }

    // Gestiona la creación de una cuenta nueva con los datos personales y el rol elegido.
    fun registrar(
        email: String, pass: String,
        nombre: String, rol: String, huerta: String
    ) {
        estado.value = Estado.Cargando
        viewModelScope.launch {
            val r = repo.registrarEmail(email, pass, nombre, rol, huerta)
            estado.value = if (r.isSuccess) {
                val u = r.getOrThrow()
                Estado.Exito(u, u.rol)
            } else {
                Estado.Error(traducir(r.exceptionOrNull()?.message))
            }
        }
    }

    // Gestiona el acceso simplificado mediante una cuenta de Google.
    fun loginConGoogle(idToken: String) {
        estado.value = Estado.Cargando
        viewModelScope.launch {
            val r = repo.loginGoogle(idToken)
            estado.value = if (r.isSuccess) {
                val u = r.getOrThrow()
                Estado.Exito(u, u.rol)
            } else {
                Estado.Error("Error al iniciar sesión con Google")
            }
        }
    }

    // Función auxiliar que cambia los mensajes de error del sistema por frases claras
    // como "Contraseña incorrecta" o "Sin conexión".
    private fun traducir(msg: String?) = when {
        msg == null                               -> "Error desconocido"
        msg.contains("no user record")           -> "No existe cuenta con ese email"
        msg.contains("password is invalid")      -> "Contraseña incorrecta"
        msg.contains("email address is already") -> "Ese email ya está registrado"
        msg.contains("network")                  -> "Sin conexión a internet"
        else                                      -> "Error: $msg"
    }
}