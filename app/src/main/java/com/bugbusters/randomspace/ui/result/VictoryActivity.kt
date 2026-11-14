package com.bugbusters.randomspace.ui.result
import com.bugbusters.randomspace.ui.game.GameActivity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.drawToBitmap
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.ui.menu.MenuDesplegable
import com.bugbusters.randomspace.ui.welcome.MainActivity
import android.animation.ValueAnimator
import com.bugbusters.randomspace.ui.AnimacionNumeros
import com.bugbusters.randomspace.ui.AnimacionVistas
import android.view.ViewGroup
import com.bugbusters.randomspace.ui.EstiloParticulas
import com.bugbusters.randomspace.ui.ParticulasView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de victoria: premio gordo, bote ganado y récord guardado.
 */
class VictoryActivity : AppCompatActivity() {

    // Cuentas de números en marcha: se cancelan al cerrar la pantalla
    private val cuentas = mutableListOf<ValueAnimator>()

    // Animaciones de números que faltan por terminar (cuenta y giro de moneda)
    private var pendientes = 0

    // Captura de la pantalla hecha al pulsar el icono, a la espera de que el jugador elija dónde guardarla
    private var capturaPendiente: Bitmap? = null

    // Selector de archivos del sistema: el jugador elige la carpeta y el nombre
    private val selectorArchivo = registerForActivityResult(
        ActivityResultContracts.CreateDocument(TIPO_CAPTURA)
    ) { uri ->
        val captura = capturaPendiente
        capturaPendiente = null
        if (uri != null && captura != null) guardarCaptura(uri, captura)
    }

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

        // Icono de cámara: guarda una imagen de la pantalla donde elija el jugador
        findViewById<View>(R.id.btnCaptura).setOnClickListener { pedirCaptura() }

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
            // Acciones al ganar (notificación, calendario, ubicación...): solo la primera vez, no al rotar
            AccionesVictoria.alGanar(this, datosVictoria())
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
    // Al acabar cada cuenta gira su moneda; cuando acaba todo, VOLVER y la cámara se activan y aparece ¡GUARDADO!
    private fun animarNumeros() {
        val saldoFinal = intent.getIntExtra(GameActivity.EXTRA_SALDO, 0)
        val saldoAntes = intent.getIntExtra(GameActivity.EXTRA_SALDO_ANTES, saldoFinal)
        val premio = intent.getIntExtra(GameActivity.EXTRA_PREMIO, 0)

        val volver = findViewById<View>(R.id.btnVolver)
        val capturar = findViewById<View>(R.id.btnCaptura)
        val guardado = findViewById<View>(R.id.tvGuardado)
        volver.isEnabled = false
        capturar.isEnabled = false
        capturar.alpha = 0f
        guardado.alpha = 0f
        pendientes = 2
        val alTerminar: () -> Unit = {
            pendientes--
            if (pendientes == 0) {
                volver.isEnabled = true
                capturar.isEnabled = true
                AnimacionVistas.aparecer(guardado)
                AnimacionVistas.aparecer(capturar, 150)
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

    // Datos de la victoria que usan las acciones (cada una coge lo que necesite)
    private fun datosVictoria() = DatosVictoria(
        fecha = System.currentTimeMillis(),
        boteBase = intent.getIntExtra(GameActivity.EXTRA_BOTE_BASE, 0),
        multiplicador = intent.getIntExtra(GameActivity.EXTRA_MULTIPLICADOR, 1),
        premio = intent.getIntExtra(GameActivity.EXTRA_PREMIO, 0),
        saldoFinal = intent.getIntExtra(GameActivity.EXTRA_SALDO, 0)
    )

    // Hace una imagen de la pantalla (sin el icono ni el botón VOLVER) y abre el selector para elegir dónde guardarla
    private fun pedirCaptura() {
        val ocultas = listOf<View>(findViewById(R.id.btnCaptura), findViewById(R.id.zonaBotones))
        ocultas.forEach { it.visibility = View.INVISIBLE }
        capturaPendiente = try {
            findViewById<View>(R.id.main).drawToBitmap()
        } finally {
            ocultas.forEach { it.visibility = View.VISIBLE }
        }
        selectorArchivo.launch(nombreCaptura())
    }

    // Nombre sugerido para el archivo, p. ej. random_space_victoria_20261114_213045.webp
    private fun nombreCaptura(): String {
        val fecha = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "random_space_victoria_$fecha.webp"
    }

    // Comprime la imagen en WebP y la escribe en el archivo elegido, fuera del hilo principal
    @Suppress("DEPRECATION")
    private fun guardarCaptura(uri: Uri, captura: Bitmap) {
        Completable.fromAction {
            val formato = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                Bitmap.CompressFormat.WEBP
            }
            val salida = contentResolver.openOutputStream(uri) ?: error("No se pudo abrir el archivo")
            salida.use { check(captura.compress(formato, CALIDAD_CAPTURA, it)) { "No se pudo comprimir la captura" } }
        }
            .doFinally { captura.recycle() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { Toast.makeText(this, R.string.captura_guardada, Toast.LENGTH_SHORT).show() },
                { error ->
                    Log.e(TAG, "No se pudo guardar la captura", error)
                    Toast.makeText(this, R.string.captura_error, Toast.LENGTH_SHORT).show()
                }
            )
    }

    private companion object {
        const val TAG = "VictoryActivity"
        const val TIPO_CAPTURA = "image/webp"
        const val CALIDAD_CAPTURA = 90   // 0-100: más alto, mejor imagen y más peso
    }
}