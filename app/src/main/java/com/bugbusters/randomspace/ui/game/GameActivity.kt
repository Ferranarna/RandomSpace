package com.bugbusters.randomspace.ui.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.game.ResultadoTirada
import com.bugbusters.randomspace.game.Tragaperras
import com.bugbusters.randomspace.model.EstadoPartida
import com.bugbusters.randomspace.ui.menu.MenuDesplegable
import com.bugbusters.randomspace.ui.result.DefeatActivity
import com.bugbusters.randomspace.ui.result.VictoryActivity
import java.util.Locale

/**
 * Pantalla de la partida: bote, rodillos, saldo, apuesta y botón de girar.
 * Toda la lógica está en game/Tragaperras; esta pantalla solo la usa y pinta el resultado.
 */
class GameActivity : AppCompatActivity() {

    // TODO: cuando exista la BD, crear la tragaperras con el saldo y el bote guardados
    private val tragaperras = Tragaperras()

    private lateinit var rodillos: List<ImageView>

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

        rodillos = listOf(
            findViewById(R.id.rodillo1),
            findViewById(R.id.rodillo2),
            findViewById(R.id.rodillo3)
        )

        // Botones + y -: la tragaperras decide si se puede cambiar la apuesta
        findViewById<View>(R.id.btnMas).setOnClickListener {
            tragaperras.aumentarApuesta()
            actualizarMarcadores()
        }
        findViewById<View>(R.id.btnMenos).setOnClickListener {
            tragaperras.disminuirApuesta()
            actualizarMarcadores()
        }

        findViewById<View>(R.id.btnGirar).setOnClickListener { girar() }

        // Botón VOLVER: cierra la pantalla y vuelve al inicio
        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }

        // Estado inicial: mensaje de bienvenida y sin resultado
        findViewById<TextView>(R.id.tvMensaje).setText(R.string.mensaje_bienvenida)
        findViewById<TextView>(R.id.tvResultadoTitulo).text = ""
        findViewById<TextView>(R.id.tvResultado).text = ""
        actualizarMarcadores()
    }

    private fun girar() {
        // Se guarda antes de girar porque si hay victoria o derrota la tragaperras reinicia el bote
        val boteAntes = tragaperras.bote
        val resultado = tragaperras.girar()

        rodillos.zip(resultado.rodillos).forEach { (vista, simbolo) ->
            vista.setImageResource(simbolo.imagen())
        }
        mostrarResultado(resultado)
        actualizarMarcadores()

        when (resultado.estado) {
            EstadoPartida.VICTORIA -> abrirVictoria(resultado, boteAntes)
            EstadoPartida.DERROTA -> abrirDerrota(resultado, boteAntes)
            EstadoPartida.EN_CURSO -> Unit
        }
    }

    //Bote, saldo y apuesta actuales.
    private fun actualizarMarcadores() {
        findViewById<TextView>(R.id.tvBote).text = formatear(tragaperras.bote)
        findViewById<TextView>(R.id.tvSaldo).text = formatear(tragaperras.saldo)
        findViewById<TextView>(R.id.tvApuesta).text = tragaperras.apuesta.toString()
    }

    // Frase de la combinación y monedas ganadas (amarillo) o perdidas (rojo).
    private fun mostrarResultado(resultado: ResultadoTirada) {
        findViewById<TextView>(R.id.tvMensaje).setText(resultado.combinacion.texto())

        val (titulo, color) = when {
            resultado.variacion > 0 -> R.string.resultado_ganaste to R.color.yellow
            resultado.variacion < 0 -> R.string.resultado_perdiste to R.color.red
            else -> R.string.resultado_empate to R.color.white
        }
        findViewById<TextView>(R.id.tvResultadoTitulo).setText(titulo)
        findViewById<TextView>(R.id.tvResultado).apply {
            text = if (resultado.variacion == 0) "" else formatearConSigno(resultado.variacion)
            setTextColor(ContextCompat.getColor(this@GameActivity, color))
        }
    }

    private fun abrirVictoria(resultado: ResultadoTirada, boteAntes: Int) {
        val intent = Intent(this, VictoryActivity::class.java)
            .putExtra(EXTRA_BOTE_BASE, boteAntes)
            .putExtra(EXTRA_MULTIPLICADOR, resultado.variacion / boteAntes)
            .putExtra(EXTRA_PREMIO, resultado.variacion)
            .putExtra(EXTRA_SALDO, resultado.saldoDespues)
        startActivity(intent)
    }

    private fun abrirDerrota(resultado: ResultadoTirada, boteAntes: Int) {
        val intent = Intent(this, DefeatActivity::class.java)
            .putExtra(EXTRA_BOTE_BASE, boteAntes)
            .putExtra(EXTRA_NUEVO_BOTE, tragaperras.bote)
            .putExtra(EXTRA_SALDO, resultado.saldoDespues)
        startActivity(intent)
    }

    companion object {
        // Claves de los datos que se pasan a las pantallas de victoria y derrota
        const val EXTRA_BOTE_BASE = "bote_base"
        const val EXTRA_MULTIPLICADOR = "multiplicador"
        const val EXTRA_PREMIO = "premio"
        const val EXTRA_NUEVO_BOTE = "nuevo_bote"
        const val EXTRA_SALDO = "saldo"

        /** 1300 -> "1.300" */
        fun formatear(monedas: Int): String = String.format(Locale.GERMANY, "%,d", monedas)

        /** 600 -> "+600", -150 -> "-150" */
        fun formatearConSigno(monedas: Int): String = String.format(Locale.GERMANY, "%+,d", monedas)
    }
}
