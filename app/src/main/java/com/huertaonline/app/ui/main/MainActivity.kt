package com.huertaonline.app.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.huertaonline.app.databinding.ActivityMainBinding

// Configura la estructura básica de la aplicación y personaliza el menú de acceso rápido
// según el perfil del usuario que haya iniciado sesión.
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuramos el sistema de navegación.
        // El "navHost" es el espacio donde irán apareciendo los diferentes fragmentos (Catálogo, Perfil, etc.).
        val navHost = supportFragmentManager
            .findFragmentById(com.huertaonline.app.R.id.nav_host_fragment)
                as NavHostFragment
        val navController = navHost.navController

        // Enlazamos la barra de navegación inferior con el controlador para que,
        // al pulsar un icono, la aplicación cambie de pantalla automáticamente.
        binding.bottomNav.setupWithNavController(navController)

        // ── Personalización según el perfil ──

        // Recuperamos el rol del usuario (por defecto es "consumidor").
        val rol = intent.getStringExtra("rol") ?: "consumidor"
        val menu = binding.bottomNav.menu

        // Si el usuario es un productor (vendedor), le mostramos la gestión de sus productos.
        menu.findItem(com.huertaonline.app.R.id.nav_mis_productos)?.isVisible = (rol == "productor")

        // Si el usuario es un consumidor (comprador), le mostramos sus pedidos y el carrito.
        menu.findItem(com.huertaonline.app.R.id.nav_pedidos)?.isVisible     = (rol == "consumidor")
        menu.findItem(com.huertaonline.app.R.id.nav_carrito)?.isVisible     = (rol == "consumidor")
    }
}
// Nota: Las opciones de cerrar sesión no están aquí, se encuentran dentro de la pantalla de Perfil.