package com.huertaonline.app.data.model

// Almacena la información personal de cada usuario y define qué tipo de acceso
// tendrá dentro de la aplicación según su función.
data class Usuario(
    val uid: String                = "", // Identificador único de seguridad del usuario.
    val nombre: String             = "", // Nombre y apellidos de la persona.
    val email: String              = "", // Dirección de correo electrónico de contacto.

    // Define el tipo de usuario. Si es "consumidor" compra productos;
    // si es "productor" puede gestionar sus ventas y huerta.
    val rol: String                = "consumidor",

    val telefono: String           = "", // Número de contacto telefónico.
    val direccion: String          = "", // Domicilio para entregas o recogidas.
    val fotoUrl: String            = "", // Enlace a la imagen de perfil del usuario.

    // Información específica que solo se completa si el usuario es un productor.
    val nombreHuerta: String       = "", // Nombre comercial de la explotación agrícola.
    val descripcionHuerta: String  = ""  // Breve reseña sobre la actividad o historia de la huerta.
)