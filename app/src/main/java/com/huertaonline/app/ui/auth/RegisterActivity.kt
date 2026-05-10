package com.huertaonline.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.huertaonline.app.databinding.ActivityRegisterBinding
import com.huertaonline.app.ui.main.MainActivity

// Gestiona el formulario de alta, adaptando los campos según si el usuario se registra
// como cliente particular o como dueño de una huerta.
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val vm: AuthViewModel by viewModels()
    private var rolSeleccionado = "consumidor" // Valor por defecto al abrir la pantalla.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Prepara la lista desplegable para elegir entre "consumidor" o "productor".
        val roles = arrayOf("consumidor", "productor")
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRol.adapter = adapter

        // Detecta qué opción se elige en el desplegable.
        // Si elige "productor", muestra el campo extra para escribir el nombre de la huerta.
        binding.spinnerRol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                rolSeleccionado = roles[pos]
                binding.layoutNombreHuerta.visibility =
                    if (rolSeleccionado == "productor") View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // Configuración del botón para finalizar el registro.
        binding.btnRegistrar.setOnClickListener {
            val nombre  = binding.etNombre.text.toString().trim()
            val email   = binding.etEmail.text.toString().trim()
            val pass    = binding.etPassword.text.toString()
            val pass2   = binding.etPassword2.text.toString()
            val huerta  = binding.etNombreHuerta.text.toString().trim()

            // Filtros de validación para asegurar que los datos son correctos.
            when {
                nombre.isEmpty() || email.isEmpty() || pass.isEmpty() ->
                    Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                pass != pass2 ->
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                pass.length < 6 ->
                    Toast.makeText(this, "Mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                rolSeleccionado == "productor" && huerta.isEmpty() ->
                    Toast.makeText(this, "Indica el nombre de tu huerta", Toast.LENGTH_SHORT).show()
                // Si todo está bien, envía la información al motor de autenticación.
                else -> vm.registrar(email, pass, nombre, rolSeleccionado, huerta)
            }
        }

        // Permite volver a la pantalla de login si el usuario ya tiene una cuenta.
        binding.tvYaTengoCuenta.setOnClickListener { finish() }

        // Vigila el estado del proceso de registro.
        vm.estado.observe(this) { estado ->
            // Muestra la barra de carga mientras el sistema procesa el alta.
            binding.progressBar.visibility =
                if (estado is AuthViewModel.Estado.Cargando) View.VISIBLE else View.GONE

            when (estado) {
                // Si el registro funciona, salta directamente a la pantalla principal.
                is AuthViewModel.Estado.Exito -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("rol", estado.rol)
                    })
                }
                // Si hay un fallo (email repetido, etc.), muestra el mensaje de error.
                is AuthViewModel.Estado.Error ->
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }
}