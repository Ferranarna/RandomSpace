package com.bugbusters.randomspace.ui.name

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.RandomSpaceApp
import com.bugbusters.randomspace.game.ReglasJuego
import com.bugbusters.randomspace.ui.welcome.MainActivity
import android.text.InputFilter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * Primera pantalla: el jugador escribe su nombre y pasa a la pantalla de inicio.
 * Si ya hay un jugador guardado, se salta y va directa al inicio.
 */
class NameActivity : AppCompatActivity() {

    private val repositorio by lazy { (application as RandomSpaceApp).repositorio }

    // Consultas a la BD en marcha: se cancelan en onDestroy para no tocar una pantalla ya cerrada
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

        // Si el jugador ya existe en la base de datos, se salta esta pantalla y va directo al inicio
        suscripciones.add(
            repositorio.obtenerJugador()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { jugador -> abrirInicio(jugador.nombre) },
                    { error -> Log.e(TAG, "No se pudo leer el jugador", error) },
                    { /* no hay jugador: se queda en esta pantalla */ }
                )
        )

        // Botón ¡EMPEZAR!: comprueba que hay nombre, guarda el jugador y abre la pantalla de inicio
        val btnEmpezar = findViewById<android.view.View>(R.id.btnEmpezar)
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
