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

        // ── Configuración Dinámica de Navegación ──

        if (rol == "productor") {
            // Si es productor, ocultamos catálogo y carrito, y mostramos gestión de productos.
            menu.findItem(com.huertaonline.app.R.id.nav_catalogo)?.isVisible = false
            menu.findItem(com.huertaonline.app.R.id.nav_carrito)?.isVisible = false
            menu.findItem(com.huertaonline.app.R.id.nav_mis_productos)?.isVisible = true
            
            // Cambiamos el título de "Pedidos" para que sea más claro para el productor
            menu.findItem(com.huertaonline.app.R.id.nav_pedidos)?.title = "Ventas"

            // Si el productor entra y está en el catálogo (que es el inicio por defecto), 
            // lo redirigimos a sus productos.
            if (navController.currentDestination?.id == com.huertaonline.app.R.id.nav_catalogo) {
                navController.navigate(com.huertaonline.app.R.id.nav_mis_productos)
            }
        } else {
            // Si es consumidor, mostramos sus opciones habituales.
            menu.findItem(com.huertaonline.app.R.id.nav_mis_productos)?.isVisible = false
            menu.findItem(com.huertaonline.app.R.id.nav_pedidos)?.isVisible = true
            menu.findItem(com.huertaonline.app.R.id.nav_carrito)?.isVisible = true
        }

        // Manejamos la navegación manualmente para asegurar que cada botón lleve a la pantalla correcta según el rol.
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.huertaonline.app.R.id.nav_catalogo -> {
                    navController.navigate(com.huertaonline.app.R.id.nav_catalogo)
                    true
                }
                com.huertaonline.app.R.id.nav_carrito -> {
                    navController.navigate(com.huertaonline.app.R.id.nav_carrito)
                    true
                }
                com.huertaonline.app.R.id.nav_pedidos -> {
                    val destino = if (rol == "productor") 
                        com.huertaonline.app.R.id.nav_pedidos_productor 
                    else 
                        com.huertaonline.app.R.id.nav_pedidos
                    navController.navigate(destino)
                    true
                }
                com.huertaonline.app.R.id.nav_mis_productos -> {
                    navController.navigate(com.huertaonline.app.R.id.nav_mis_productos)
                    true
                }
                com.huertaonline.app.R.id.nav_perfil -> {
                    navController.navigate(com.huertaonline.app.R.id.nav_perfil)
                    true
                }
                else -> false
            }
        }
    }
}
// Nota: Las opciones de cerrar sesión no están aquí, se encuentran dentro de la pantalla de Perfil.