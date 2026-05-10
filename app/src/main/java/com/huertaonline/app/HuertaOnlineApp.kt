package com.huertaonline.app

import android.app.Application
import com.google.firebase.FirebaseApp

// Garantiza que la conexión con la base de datos, el sistema de usuarios
// y el almacenamiento de fotos estén listos desde el inicio.
class HuertaOnlineApp : Application() {

    // Este método se ejecuta una sola vez, justo cuando la app nace en la memoria del móvil.
    override fun onCreate() {
        super.onCreate()

        // Inicializa Firebase. Sin esta línea, cualquier intento de login
        // o de ver productos causaría que la app se cerrara por error.
        FirebaseApp.initializeApp(this)
    }
}