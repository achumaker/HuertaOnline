package com.huertaonline.app.data.model

// Contiene toda la información detallada de un artículo que se muestra en la tienda,
// incluyendo datos de stock, origen y reputación.
data class Producto(
    val id: String               = "", // Identificador único de este producto.
    val nombre: String           = "", // Nombre del artículo para mostrar al público.
    val descripcion: String      = "", // Texto detallado con las características del producto.
    val precio: Double           = 0.0, // Coste económico por unidad o medida.
    val unidad: String           = "", // Tipo de medida (ej: "kg", "litro", "unidad").
    val stock: Int               = 0, // Cantidad de existencias disponibles actualmente.
    val categoria: String        = "", // Grupo al que pertenece (ej: "frutas", "verduras").
    val productorId: String      = "", // Identificador del agricultor o proveedor.
    val productorNombre: String  = "", // Nombre comercial de quien produce el artículo.
    val imagenUrl: String        = "", // Enlace a la fotografía del producto.
    val mediaValoracion: Double  = 0.0, // Nota media basada en las opiniones de clientes.
    val activo: Boolean          = true // Indica si el producto está a la venta o retirado.
)