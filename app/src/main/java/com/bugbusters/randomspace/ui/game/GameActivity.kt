package com.bugbusters.randomspace.ui.game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.RandomSpaceApp
import com.bugbusters.randomspace.game.ReglasJuego
import com.bugbusters.randomspace.game.ResultadoTirada
import com.bugbusters.randomspace.game.Tragaperras
import com.bugbusters.randomspace.model.EstadoPartida
import com.bugbusters.randomspace.model.Partida
import com.bugbusters.randomspace.ui.menu.MenuDesplegable
import com.bugbusters.randomspace.ui.result.DefeatActivity
import com.bugbusters.randomspace.ui.result.VictoryActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.bugbusters.randomspace.ui.welcome.MainActivity

/**
 * Pantalla de la partida: bote, rodillos, saldo, apuesta y botón de girar.
 * Toda la lógica está en game/Tragaperras; esta pantalla solo la usa, pinta el resultado
 * y pide al repositorio que lo guarde.
 */
class GameActivity : AppCompatActivity() {

    private val repositorio by lazy { (application as RandomSpaceApp).repositorio }

    // Lecturas en marcha: se cancelan en onDestroy para no tocar una pantalla ya cerrada
    private val suscripciones = CompositeDisposable()

    // Se crea con el saldo y el bote guardados cuando llegan de la BD
    private lateinit var tragaperras: Tragaperras

    // Partida EN_CURSO en la BD. null mientras se carga o mientras se cierra una partida
    private var partida: Partida? = null

    private lateinit var rodillos: List<ImageView>
    private lateinit var botonesJuego: List<View>

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

        // Hasta que no llegan los datos de la BD no se puede jugar
        botonesJuego = listOf(findViewById(R.id.btnMas), findViewById(R.id.btnMenos), findViewById(R.id.btnGirar))
        activarJuego(false)

        // Botón VOLVER: cierra la pantalla y vuelve al inicio
        findViewById<View>(R.id.btnVolver).setOnClickListener { MainActivity.irAlInicio(this) }

        // Estado inicial: "¡BIENVENIDO!" arriba, "¡PULSA GIRAR!" abajo y sin moneda
        findViewById<TextView>(R.id.tvMensaje).setText(R.string.mensaje_bienvenida)
        findViewById<TextView>(R.id.tvResultadoTitulo).setText(R.string.mensaje_pulsa_girar)
        findViewById<TextView>(R.id.tvResultado).text = ""
        findViewById<View>(R.id.ivMonedaResultado).visibility = View.INVISIBLE

        cargarPartida()
    }

    // Al volver desde Victoria o Derrota se muestran ya el bote y el saldo de la partida nueva
    override fun onResume() {
        super.onResume()
        if (::tragaperras.isInitialized) actualizarMarcadores()
    }

    override fun onDestroy() {
        suscripciones.clear()
        super.onDestroy()
    }

    // Recupera la partida en curso (o crea una) y arranca la tragaperras con su saldo y el bote activo
    private fun cargarPartida() {
        suscripciones.add(
            repositorio.cargarPartida(ReglasJuego.SALDO_INICIAL)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { datos ->
                        partida = datos.partida
                        tragaperras = Tragaperras(
                            saldoInicial = datos.partida.saldoFinal,
                            boteInicial = datos.bote.cantidad
                        )
                        actualizarMarcadores()
                        activarJuego(true)
                    },
                    { error -> mostrarError("No se pudo cargar la partida", error) }
                )
        )
    }

    private fun girar() {
        val partidaActual = partida ?: return

        // Se guarda antes de girar porque si hay victoria o derrota la tragaperras reinicia el bote
        val boteAntes = tragaperras.bote
        val resultado = tragaperras.girar()

        rodillos.zip(resultado.rodillos).forEach { (vista, simbolo) ->
            vista.setImageResource(simbolo.imagen())
        }
        mostrarResultado(resultado)
        // En victoria o derrota la tragaperras ya se ha reiniciado: esos marcadores se actualizan al volver a esta pantalla
        if (resultado.estado == EstadoPartida.EN_CURSO) actualizarMarcadores()
        guardar(partidaActual, resultado)

        when (resultado.estado) {
            EstadoPartida.VICTORIA -> abrirVictoria(resultado, boteAntes)
            EstadoPartida.DERROTA -> abrirDerrota(resultado, boteAntes)
            EstadoPartida.EN_CURSO -> Unit
        }
    }

    /**
     * Guarda la tirada en la BD. Las escrituras NO van a [suscripciones]: si se sale de la pantalla
     * deben terminar igualmente (cancelarlas perdería el saldo).
     */
    private fun guardar(partidaActual: Partida, resultado: ResultadoTirada) {
        when (resultado.estado) {
            EstadoPartida.EN_CURSO -> {
                // La copia en memoria se actualiza al momento: la siguiente tirada ya parte de ella
                val actualizada = repositorio.aplicarTirada(partidaActual, resultado.saldoDespues)
                partida = actualizada
                repositorio.guardarTirada(actualizada)
                    .subscribe({}, { error -> Log.e(TAG, "No se pudo guardar la tirada", error) })
            }
            EstadoPartida.DERROTA -> terminarPartida(
                repositorio.terminarConDerrota(partidaActual, resultado.saldoDespues, ReglasJuego.SALDO_INICIAL)
            )
            EstadoPartida.VICTORIA -> terminarPartida(
                repositorio.terminarConVictoria(
                    partidaActual, resultado.saldoDespues,
                    ReglasJuego.SALDO_INICIAL, ReglasJuego.BOTE_INICIAL, ReglasJuego.INCREMENTO_BOTE
                )
            )
        }
    }

    // Cierra la partida en la BD; hasta que no llega la nueva (con su id) no se puede volver a girar
    private fun terminarPartida(operacion: Single<Partida>) {
        partida = null
        activarJuego(false)
        operacion
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { nueva ->
                    partida = nueva
                    activarJuego(true)
                },
                { error -> mostrarError("No se pudo cerrar la partida", error) }
            )
    }

    private fun activarJuego(activo: Boolean) {
        botonesJuego.forEach { it.isEnabled = activo }
    }

    private fun mostrarError(mensaje: String, error: Throwable) {
        Log.e(TAG, mensaje, error)
        if (!isDestroyed) Toast.makeText(this, R.string.error_base_datos, Toast.LENGTH_SHORT).show()
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
        findViewById<View>(R.id.ivMonedaResultado).visibility =
            if (resultado.variacion == 0) View.INVISIBLE else View.VISIBLE
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
        private const val TAG = "GameActivity"

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

        /** Fecha guardada en la BD (epoch millis) -> "19/10/2026" */
        fun formatearFecha(millis: Long): String =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
    }
}
