package com.bugbusters.randomspace.ui.result
import com.bugbusters.randomspace.ui.game.GameActivity

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
 * Pantalla de victoria: premio gordo, bote ganado y récord guardado.
 */
class VictoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pantalla completa, con margen para que las barras del sistema no tapen nada
        enableEdgeToEdge()
        setContentView(R.layout.activity_victory)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón de menú de la barra superior
        findViewById<View>(R.id.btnMenu).setOnClickListener { v -> MenuDesplegable.mostrar(this, v) }

        // Botón VOLVER: cierra la pantalla
        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }

        // Datos que manda GameActivity al ganar
        mostrarDatos(
            boteBase = GameActivity.formatear(intent.getIntExtra(GameActivity.EXTRA_BOTE_BASE, 0)),
            multiplicador = intent.getIntExtra(GameActivity.EXTRA_MULTIPLICADOR, 1),
            premio = GameActivity.formatear(intent.getIntExtra(GameActivity.EXTRA_PREMIO, 0)),
            saldo = GameActivity.formatear(intent.getIntExtra(GameActivity.EXTRA_SALDO, 0))
        )

        // Título en verde
        findViewById<TextView>(R.id.tvTitulo).apply {
            setText(R.string.victoria_titulo)
            setTextColor(androidx.core.content.ContextCompat.getColor(this@VictoryActivity, R.color.green))
        }
    }

    /**
     * Rellena los números de la pantalla. Los textos fijos están en strings.xml.
     *
     * @param boteBase bote antes de multiplicar (ya formateado, p. ej. "1.000")
     * @param multiplicador apuesta / 10 (1, 2 o 3)
     * @param premio boteBase x multiplicador (ya formateado, p. ej. "3.000")
     * @param guardado puntuación guardada (ya formateada)
     * @param saldo saldo del jugador (ya formateado)
     */
    private fun mostrarDatos(
        boteBase: String, multiplicador: Int, premio: String,
        saldo: String
    ) {
        findViewById<TextView>(R.id.tvBoteBase).text = boteBase
        findViewById<TextView>(R.id.tvMultiplicador).text = getString(R.string.multiplicador, multiplicador)
        findViewById<TextView>(R.id.tvPremio).text = premio
        findViewById<TextView>(R.id.tvSaldo).text = saldo
    }
}