package com.huertaonline.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.huertaonline.app.R
import com.huertaonline.app.databinding.ActivityLoginBinding
import com.huertaonline.app.ui.main.MainActivity

// Controla la interfaz visual donde el usuario introduce sus datos, gestiona los botones
// de acceso y decide qué pantalla mostrar a continuación.
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val vm: AuthViewModel by viewModels()
    private lateinit var googleClient: GoogleSignInClient

    // Gestor del resultado al intentar entrar con Google.
    // Si la cuenta es válida, le pasa el testigo al motor de autenticación.
    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val cuenta = GoogleSignIn
                .getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            vm.loginConGoogle(cuenta.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Error Google (${e.statusCode})", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificación inicial: si el usuario ya dejó su sesión abierta,
        // entra automáticamente sin pedir datos de nuevo.
        vm.comprobarSesion { rol ->
            if (rol != null) irAMain(rol)
        }

        // Configuración de los servicios de Google para permitir el acceso con un toque.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        // Acción del botón de login tradicional.
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString()

            // Validación de seguridad básica: no permite enviar campos vacíos.
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.login(email, pass)
        }

        // Lanza el selector de cuentas de Google al pulsar el botón correspondiente.
        binding.btnGoogle.setOnClickListener {
            googleLauncher.launch(googleClient.signInIntent)
        }

        // Navega hacia la pantalla de registro si el usuario aún no tiene cuenta.
        binding.tvRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Observador del estado: reacciona según lo que ocurra en el motor de acceso.
        vm.estado.observe(this) { estado ->
            // Muestra u oculta la barra de carga según si el sistema está trabajando.
            binding.progressBar.visibility =
                if (estado is AuthViewModel.Estado.Cargando) View.VISIBLE else View.GONE

            when (estado) {
                // Si el acceso es correcto, redirige al menú principal.
                is AuthViewModel.Estado.Exito -> irAMain(estado.rol)
                
                // Si es un usuario nuevo de Google, preguntamos el rol antes de seguir.
                is AuthViewModel.Estado.NuevoUsuarioGoogle -> mostrarDialogoRol(estado)

                // Si hay un error, muestra un aviso flotante con la explicación.
                is AuthViewModel.Estado.Error ->
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    private fun mostrarDialogoRol(info: AuthViewModel.Estado.NuevoUsuarioGoogle) {
        val roles = arrayOf("Consumidor", "Productor")
        var rolElegido = "consumidor"
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Selecciona tu perfil")
            .setSingleChoiceItems(roles, 0) { _, which ->
                rolElegido = roles[which].lowercase()
            }
            .setPositiveButton("Continuar") { _, _ ->
                if (rolElegido == "productor") {
                    mostrarDialogoNombreHuerta(info)
                } else {
                    vm.completarRegistroGoogle(info.uid, info.nombre, info.email, info.foto, "consumidor", "")
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun mostrarDialogoNombreHuerta(info: AuthViewModel.Estado.NuevoUsuarioGoogle) {
        val input = android.widget.EditText(this)
        input.hint = "Ej: Huerta de Juan"
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Nombre de tu Huerta")
            .setMessage("Indica cómo se llama tu negocio agrícola:")
            .setView(input)
            .setPositiveButton("Crear perfil") { _, _ ->
                val nombre = input.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    mostrarDialogoNombreHuerta(info) // Reabrir si está vacío
                } else {
                    vm.completarRegistroGoogle(info.uid, info.nombre, info.email, info.foto, "productor", nombre)
                }
            }
            .setCancelable(false)
            .show()
    }

    // Función para saltar a la pantalla principal, cerrando la de acceso
    // para que el usuario no pueda volver atrás al pulsar "atrás".
    private fun irAMain(rol: String) {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("rol", rol)
        })
    }
}