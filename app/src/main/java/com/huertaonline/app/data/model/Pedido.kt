package com.huertaonline.app.data.model

// Define los datos básicos de un producto dentro de un pedido realizado.
data class ItemPedido(
    val productoId: String      = "", // Código único del producto.
    val nombre: String          = "", // Nombre del artículo en el momento de la compra.
    val cantidad: Int           = 1,  // Unidades adquiridas.
    val precioUnitario: Double  = 0.0 // Precio por unidad acordado.
)

// Define la información general de una compra completada.
data class Pedido(
    val id: String              = "", // Código de identificación de la orden.
    val consumidorId: String    = "", // Código del usuario que realiza la compra.
    val consumidorNombre: String = "", // Nombre del cliente para el registro del pedido.
    val items: List<ItemPedido>  = emptyList(), // Lista de todos los productos incluidos.
    val total: Double           = 0.0, // Importe final a pagar.
    val estado: String          = "pendiente", // Situación del envío (ej: pendiente, enviado).
    val direccionEnvio: String  = "", // Lugar de entrega del paquete.
    val fecha: Long             = System.currentTimeMillis() // Momento exacto en que se registró.
)

// Define la estructura de las opiniones de los clientes.
data class Valoracion(
    val id: String              = "", // Código único de la reseña.
    val productoId: String      = "", // Código del producto que se está valorando.
    val consumidorId: String    = "", // Código del usuario que escribe la opinión.
    val consumidorNombre: String = "", // Nombre que se mostrará junto al comentario.
    val puntuacion: Int         = 0,  // Calificación numérica (ej: de 1 a 5).
    val comentario: String      = "", // Texto con la opinión escrita.
    val fecha: Long             = System.currentTimeMillis() // Fecha de publicación.
)