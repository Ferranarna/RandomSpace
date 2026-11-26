package com.bugbusters.randomspace.ui.name

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.RandomSpaceApp
import com.bugbusters.randomspace.game.ReglasJuego
import com.bugbusters.randomspace.model.Sesion
import com.bugbusters.randomspace.ui.welcome.MainActivity
import android.text.InputFilter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * Primera pantalla: el jugador inicia sesión con Google y después escribe su nombre.
 * Si ya ha iniciado sesión y tiene jugador guardado, se salta y va directa al inicio.
 */
class NameActivity : AppCompatActivity() {

    private val repositorio by lazy { (application as RandomSpaceApp).repositorio }
    private val sesion by lazy { (application as RandomSpaceApp).sesion }
    private val loginGoogle by lazy { LoginGoogle(this) }

    // Consultas en marcha: se cancelan en onDestroy para no tocar una pantalla ya cerrada
    private val suscripciones = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pantalla completa, con margen para que las barras del sistema no tapen nada
        enableEdgeToEdge()
        setContentView(R.layout.activity_name)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Fuerza mayúsculas al escribir, aunque el teclado no lo haga (15 = longitud máxima)
        findViewById<EditText>(R.id.etNombre).filters = arrayOf(
            InputFilter.AllCaps(),
            InputFilter.LengthFilter(15)
        )

        findViewById<View>(R.id.btnGoogle).setOnClickListener { iniciarSesion() }

        // Botón ¡EMPEZAR!: comprueba que hay nombre, guarda el jugador y abre la pantalla de inicio
        val btnEmpezar = findViewById<View>(R.id.btnEmpezar)
        btnEmpezar.setOnClickListener {
            val nombre = findViewById<EditText>(R.id.etNombre).text.toString().trim().uppercase()
            if (nombre.isEmpty()) {
                Toast.makeText(this, R.string.nombre_vacio, Toast.LENGTH_SHORT).show()
            } else {
                // Desactivado mientras se guarda: dos pulsaciones seguidas crearían dos jugadores
                btnEmpezar.isEnabled = false
                suscripciones.add(
                    repositorio.registrarJugador(
                        nombre,
                        ReglasJuego.SALDO_INICIAL,
                        ReglasJuego.BOTE_INICIAL,
                        ReglasJuego.INCREMENTO_BOTE,
                        ReglasJuego.APUESTA_MINIMA
                    )
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { abrirInicio(nombre) },
                            { error ->
                                Log.e(TAG, "No se pudo registrar el jugador", error)
                                btnEmpezar.isEnabled = true
                                Toast.makeText(this, R.string.error_base_datos, Toast.LENGTH_SHORT).show()
                            }
                        )
                )
            }
        }

        // Sin sesión se queda visible el login (así está en el layout)
        sesion.sesionActual()?.let { continuar(it) }
    }

    private fun iniciarSesion() {
        val btnGoogle = findViewById<View>(R.id.btnGoogle)
        btnGoogle.isEnabled = false
        suscripciones.add(
            loginGoogle.pedirToken()
                .flatMap { token -> sesion.iniciarSesion(token) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { nueva -> continuar(nueva) },
                    { error ->
                        btnGoogle.isEnabled = true
                        avisarErrorLogin(error)
                    }
                )
        )
    }

    private fun avisarErrorLogin(error: Throwable) {
        // Si el jugador cierra el selector no es un fallo: no se avisa
        if (error is GetCredentialCancellationException) return
        Log.e(TAG, "No se pudo iniciar sesión", error)
        val mensaje = if (error is NoCredentialException) R.string.login_sin_cuenta else R.string.login_error
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    // Con la sesión iniciada: si ya hay jugador en el móvil va al inicio; si no, pide el nombre
    private fun continuar(actual: Sesion) {
        findViewById<View>(R.id.tvLogin).visibility = View.GONE
        findViewById<View>(R.id.btnGoogle).visibility = View.GONE
        suscripciones.add(
            repositorio.obtenerJugador()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { jugador -> abrirInicio(jugador.nombre) },
                    { error ->
                        Log.e(TAG, "No se pudo leer el jugador", error)
                        Toast.makeText(this, R.string.error_base_datos, Toast.LENGTH_SHORT).show()
                    },
                    { mostrarNombre(actual) }   // no hay jugador
                )
        )
    }

    private fun mostrarNombre(actual: Sesion) {
        val campo = findViewById<EditText>(R.id.etNombre)
        // Propone el nombre de pila de Google, sin pisar lo que ya se hubiera escrito
        if (campo.text.isEmpty()) {
            campo.setText(actual.nombreGoogle.trim().substringBefore(" "))
        }
        findViewById<View>(R.id.grupoNombre).visibility = View.VISIBLE
        findViewById<View>(R.id.btnEmpezar).visibility = View.VISIBLE
    }

    override fun onDestroy() {
        suscripciones.clear()
        super.onDestroy()
    }

    private fun abrirInicio(nombre: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("nombre", nombre)
        startActivity(intent)
        finish() // así, al pulsar atrás en el inicio no se vuelve a pedir el nombre
    }

    companion object {
        private const val TAG = "NameActivity"
    }
}