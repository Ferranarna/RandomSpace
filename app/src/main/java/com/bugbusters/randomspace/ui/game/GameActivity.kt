package com.bugbusters.randomspace.ui.game

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.ui.menu.MenuDesplegable

/**
 * Pantalla de la partida: bote, rodillos, saldo, apuesta y botón de girar.
 * De momento solo tiene el diseño; la lógica del juego se conecta en los TODO.
 */
class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pantalla completa, con margen para que las barras del sistema no tapen nada
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón de menú de la barra superior
        findViewById<View>(R.id.btnMenu).setOnClickListener { v -> MenuDesplegable.mostrar(this, v) }

        // TODO: DATOS INVENTADOS A SUSTITUIR (es para ver como queda, es temporal)
        mostrarDatos(bote = "1.000", saldo = "1.200", apuesta = "10", mensaje = "¡LLUVIA DE ESTRELLAS!", resultado = "GANASTE +200")

        // TODO: subir la apuesta (temporal)
        findViewById<View>(R.id.btnMas).setOnClickListener {
            Toast.makeText(this, "+", Toast.LENGTH_SHORT).show()
        }

        // TODO: bajar la apuesta (temporal)
        findViewById<View>(R.id.btnMenos).setOnClickListener {
            Toast.makeText(this, "-", Toast.LENGTH_SHORT).show()
        }

        // TODO: girar los rodillos y calcular el resultado (temporal: abre las pantallas de resultado para verlas)
        findViewById<View>(R.id.btnGirar).setOnClickListener {
            startActivity(android.content.Intent(this, com.bugbusters.randomspace.ui.result.VictoryActivity::class.java))
        }
        findViewById<View>(R.id.btnGirar).setOnLongClickListener {
            startActivity(android.content.Intent(this, com.bugbusters.randomspace.ui.result.DefeatActivity::class.java))
            true
        }

        // Botón VOLVER: cierra la pantalla y vuelve al inicio
        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }
    }

    /**
     * Rellena los textos de la pantalla. Solo cambia textos: el diseño está en activity_game.xml.
     *
     * @param bote bote actual (ya formateado)
     * @param saldo saldo del jugador (ya formateado)
     * @param apuesta apuesta elegida
     * @param mensaje texto de la combinación obtenida
     * @param resultado monedas ganadas o perdidas, con signo (p. ej. "+200")
     */
    private fun mostrarDatos(bote: String, saldo: String, apuesta: String, mensaje: String, resultado: String) {
        findViewById<TextView>(R.id.tvBote).text = bote
        findViewById<TextView>(R.id.tvSaldo).text = saldo
        findViewById<TextView>(R.id.tvApuesta).text = apuesta
        findViewById<TextView>(R.id.tvMensaje).text = mensaje
        findViewById<TextView>(R.id.tvResultado).text = resultado
    }
}