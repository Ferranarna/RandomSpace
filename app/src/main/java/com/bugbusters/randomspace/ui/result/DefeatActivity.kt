package com.bugbusters.randomspace.ui.result

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.ui.menu.MenuDesplegable

/**
 * Pantalla de derrota: saldo insuficiente, puntuación guardada y nuevo bote.
 */
class DefeatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pantalla completa, con margen para que las barras del sistema no tapen nada
        enableEdgeToEdge()
        setContentView(R.layout.activity_defeat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón de menú de la barra superior
        findViewById<View>(R.id.btnMenu).setOnClickListener { v -> MenuDesplegable.mostrar(this, v) }

        // Botón VOLVER: cierra la pantalla
        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }

        // TODO: DATOS INVENTADOS A SUSTITUIR (es para ver como queda, es temporal)
        mostrarDatos(puntuacion = "8", incremento = "+100", nuevoBote = "1.100")

        // Título en rojo
        findViewById<TextView>(R.id.tvTitulo).apply {
            setText(R.string.derrota_titulo)
            setTextColor(androidx.core.content.ContextCompat.getColor(this@DefeatActivity, R.color.red))
        }
    }

    /**
     * @param puntuacion monedas conseguidas en la partida (ya formateado)
     * @param incremento lo que sube el bote, con signo (p. ej. "+100")
     * @param nuevoBote bote resultante (ya formateado)
     */
    private fun mostrarDatos(puntuacion: String, incremento: String, nuevoBote: String) {
        findViewById<TextView>(R.id.tvPuntuacion).text = puntuacion
        findViewById<TextView>(R.id.tvIncremento).text = incremento
        findViewById<TextView>(R.id.tvNuevoBote).text = nuevoBote
    }
}