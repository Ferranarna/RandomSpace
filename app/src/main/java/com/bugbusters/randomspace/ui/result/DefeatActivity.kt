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
import com.bugbusters.randomspace.ui.welcome.MainActivity

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
        findViewById<View>(R.id.btnVolver).setOnClickListener { MainActivity.irAlInicio(this) }

        // Datos que manda GameActivity al perder
        val boteBase = intent.getIntExtra(GameActivity.EXTRA_BOTE_BASE, 0)
        val nuevoBote = intent.getIntExtra(GameActivity.EXTRA_NUEVO_BOTE, 0)
        mostrarDatos(
            boteBase = GameActivity.formatear(boteBase),
            incremento = GameActivity.formatearConSigno(nuevoBote - boteBase),
            nuevoBote = GameActivity.formatear(nuevoBote),
            saldo = GameActivity.formatear(intent.getIntExtra(GameActivity.EXTRA_SALDO, 0))
        )

        // Título en rojo
        findViewById<TextView>(R.id.tvTitulo).apply {
            setText(R.string.derrota_titulo)
            setTextColor(androidx.core.content.ContextCompat.getColor(this@DefeatActivity, R.color.red))
        }
    }

    /**
     * Rellena los números de la pantalla. Los textos fijos están en strings.xml.
     *
     * @param boteBase bote antes de perder (ya formateado, p. ej. "1.000")
     * @param incremento lo que sube el bote, con signo (p. ej. "+100")
     * @param nuevoBote bote resultante (ya formateado)
     * @param saldo saldo del jugador (ya formateado)
     */
    private fun mostrarDatos(
        boteBase: String, incremento: String, nuevoBote: String,
        saldo: String
    ) {
        findViewById<TextView>(R.id.tvBoteBase).text = boteBase
        findViewById<TextView>(R.id.tvIncremento).text = incremento
        findViewById<TextView>(R.id.tvNuevoBote).text = nuevoBote
        findViewById<TextView>(R.id.tvSaldo).text = saldo
    }
}