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
import com.bugbusters.randomspace.ui.ConfetiView
import com.bugbusters.randomspace.ui.EstiloParticulas
import com.bugbusters.randomspace.ui.ParticulasView

/**
 * Pantalla de victoria: premio gordo, bote ganado y récord guardado.
 */
class VictoryActivity : AppCompatActivity() {

    // Cuentas de números en marcha: se cancelan al cerrar la pantalla
    private val cuentas = mutableListOf<ValueAnimator>()

    // Animaciones de números que faltan por terminar (cuenta y giro de moneda)
    private var pendientes = 0

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

        // Botón VOLVER: cierra la pantalla y vuelve al inicio
        findViewById<View>(R.id.btnVolver).setOnClickListener { MainActivity.irAlInicio(this) }

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

        // Los números suben (solo la primera vez, no al rotar el móvil)
        if (savedInstanceState == null) {
            animarCabecera()
            animarNumeros()
            lanzarConfeti()
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

    // El saldo sube desde el que había antes de la tirada y el premio cuenta desde 0.
    // Al acabar cada cuenta gira su moneda; cuando acaba todo, VOLVER se activa y aparece ¡GUARDADO!
    private fun animarNumeros() {
        val saldoFinal = intent.getIntExtra(GameActivity.EXTRA_SALDO, 0)
        val saldoAntes = intent.getIntExtra(GameActivity.EXTRA_SALDO_ANTES, saldoFinal)
        val premio = intent.getIntExtra(GameActivity.EXTRA_PREMIO, 0)

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
            findViewById(R.id.tvPremio), 0, premio,
            moneda = findViewById(R.id.ivMonedaPremio), alTerminar = alTerminar
        ) { GameActivity.formatear(it) }
        cuentas += AnimacionNumeros.contar(
            findViewById(R.id.tvSaldo), saldoAntes, saldoFinal,
            moneda = findViewById(R.id.ivMonedaSaldo), alTerminar = alTerminar
        ) { GameActivity.formatear(it) }
    }

    /**
     * Rellena los números de la pantalla. Los textos fijos están en strings.xml.
     *
     * @param boteBase bote antes de multiplicar (ya formateado, p. ej. "1.000")
     * @param multiplicador apuesta / 10 (de 1 a 10)
     * @param premio boteBase x multiplicador (ya formateado, p. ej. "3.000")
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

    // Confeti de píxeles que cae por toda la pantalla
    private fun lanzarConfeti() {
        val confeti = ParticulasView(this)
        findViewById<ViewGroup>(android.R.id.content).addView(
            confeti,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        confeti.lanzar(EstiloParticulas.CONFETI)
    }
}