# 🍏 HuertaOnline - Marketplace de Proximidad

HuertaOnline es una aplicación Android nativa que conecta a los pequeños productores de la huerta murciana con el consumidor final, eliminando intermediarios y fomentando el consumo de producto local.

## 🚀 Características Principales
- **Doble Perfil de Usuario:** Productor (Gestión de catálogo y stock) y Consumidor (Exploración y compra).
- Sincronización en tiempo real mediante Firebase Firestore
- Carrito de compra con persistencia local usando Room Database
- Autenticación con email/contraseña y Google Sign-In
- Simulación de pasarela de pago con flujo completo de pedido
- Arquitectura MVVM con repositorios y corrutinas Kotlin

## 🛠️ Stack Tecnológico
- **Lenguaje:** Kotlin + Corrutinas.
- **Backend:** Firebase (Auth, Firestore, Storage).
- **Jetpack Components:** Navigation Component, ViewBinding, ViewModel, LiveData.
- **Librerías:** Glide (Carga de imágenes).

## 📋 Requisitos de Instalación
1. Clonar el repositorio.
2. Añadir el archivo `google-services.json` en la carpeta `/app`.
3. Compilar con Android Studio.
4. SDK Mínimo: API 26 (Android 8.0).

---
*Proyecto Final de Ciclo - Grado Superior en Desarrollo de Aplicaciones Multiplataforma (DAM)*
