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
import android.animation.ValueAnimator
import com.bugbusters.randomspace.ui.AnimacionNumeros
import com.bugbusters.randomspace.ui.AnimacionVistas
import android.view.ViewGroup
import com.bugbusters.randomspace.ui.EstiloParticulas
import com.bugbusters.randomspace.ui.ParticulasView

/**
 * Pantalla de derrota: saldo insuficiente, puntuación guardada y nuevo bote.
 */
class DefeatActivity : AppCompatActivity() {

    // Cuentas de números en marcha: se cancelan al cerrar la pantalla
    private val cuentas = mutableListOf<ValueAnimator>()

    // Animaciones de números  (cuenta y giro de moneda)
    private var pendientes = 0

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

        // Los números cambian (solo la primera vez, no al rotar el móvil)
        if (savedInstanceState == null) {
            animarCabecera()
            animarNumeros(boteBase, nuevoBote)
            lanzarCenizas()
        }
    }

    override fun onDestroy() {
        cuentas.forEach { it.cancel() }
        super.onDestroy()
    }

    // Título con estrellas, imagen y frase caen juntos desde arriba
    private fun animarCabecera() {
        AnimacionVistas.caer(
            listOf<View>(
                findViewById(R.id.cabeceraTitulo),
                findViewById(R.id.ivResultado),
                findViewById(R.id.tvSubtitulo)
            )
        )
    }

    // Cenizas que flotan por toda la pantalla empujadas por el viento (encima de todo, sin bloquear los toques)
    private fun lanzarCenizas() {
        val cenizas = ParticulasView(this)
        findViewById<ViewGroup>(android.R.id.content).addView(
            cenizas,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        cenizas.lanzar(EstiloParticulas.CENIZA)
    }

    // El bote sube hasta el nuevo valor y el saldo baja desde el que había antes de la tirada.
    // Al acabar cada cuenta gira su moneda; cuando acaba todo, VOLVER se activa y aparece ¡GUARDADO!
    private fun animarNumeros(boteBase: Int, nuevoBote: Int) {
        val saldoFinal = intent.getIntExtra(GameActivity.EXTRA_SALDO, 0)
        val saldoAntes = intent.getIntExtra(GameActivity.EXTRA_SALDO_ANTES, saldoFinal)

        val volver = findViewById<View>(R.id.btnVolver)
        val guardado = findViewById<View>(R.id.tvGuardado)
        volver.isEnabled = false
        guardado.alpha = 0f
        pendientes = 2
        val alTerminar: () -> Unit = {
            pendientes--
            if (pendientes == 0) {
                volver.isEnabled = true
                AnimacionVistas.aparecer(guardado)
            }
        }

        cuentas += AnimacionNumeros.contar(
            findViewById(R.id.tvNuevoBote), boteBase, nuevoBote,
            moneda = findViewById(R.id.ivMonedaNuevoBote), alTerminar = alTerminar
        ) { GameActivity.formatear(it) }
        cuentas += AnimacionNumeros.contar(
            findViewById(R.id.tvSaldo), saldoAntes, saldoFinal,
            moneda = findViewById(R.id.ivMonedaSaldo), alTerminar = alTerminar
        ) { GameActivity.formatear(it) }
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