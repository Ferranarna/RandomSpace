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
import com.bugbusters.randomspace.model.Simbolo
import android.view.animation.OvershootInterpolator
import android.animation.ValueAnimator
import androidx.core.animation.doOnEnd
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.lifecycle.Lifecycle
import com.bugbusters.randomspace.game.GeneradorSimbolos

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

    private lateinit var animador: AnimadorRodillos

    // Lo que hay que hacer cuando paren los rodillos (enseñar el resultado).
    private var pendiente: (() -> Unit)? = null
    // Saldo que se está viendo en pantalla (para contar desde ahí hasta el nuevo)
    private var saldoMostrado = 0
    // Tras una victoria o derrota los carteles quedan ocultos
    private var bienvenidaPendiente = false
    private var contador: ValueAnimator? = null


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

        animador = AnimadorRodillos(rodillos)
        animador.mostrar(listOf(Simbolo.NEPTUNO, Simbolo.HAMBURGUESA, Simbolo.SATURNO))

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
        findViewById<View>(R.id.btnVolver).setOnClickListener {
            if (!animador.girando) MainActivity.irAlInicio(this)
        }

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
        mostrarBienvenida()
        // Si se cortó un giro al salir de la pantalla, ahora se enseña su resultado
        // Si se cortó un giro al salir de la pantalla, ahora se enseña su resultado
        val accion = pendiente
        pendiente = null
        accion?.invoke()
    }

    // Si se sale a mitad del giro se corta la animación (la tirada ya está guardada en la BD)
    override fun onPause() {
        animador.cancelar()
        super.onPause()
    }

    override fun onDestroy() {
        animador.cancelar()
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
                            boteInicial = datos.bote.cantidad,
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
        if (animador.girando) return

        // Se guarda antes de girar porque si hay victoria o derrota la tragaperras reinicia el bote
        val boteAntes = tragaperras.bote
        val resultado = tragaperras.girar()

        // La BD se guarda ya: si se sale a mitad de la animación, la tirada no se pierde
        guardar(partidaActual, resultado)

        // Mientras giran no se puede tocar nada, y el resultado se enseña cuando paren los rodillos
        activarJuego(false)
        ocultarMensajes()
        pendiente = { terminarGiro(resultado, boteAntes) }
        animador.girar(resultado.rodillos, object : EscuchaGiro {
            override fun alTerminar() {
                val accion = pendiente
                pendiente = null
                accion?.invoke()
            }
        })
    }
    private fun terminarGiro(resultado: ResultadoTirada, boteAntes: Int) {
        // En victoria o derrota no hacen falta los carteles: esos datos ya se ven en su pantalla
        if (resultado.estado == EstadoPartida.EN_CURSO) mostrarResultado(resultado) else prepararBienvenida()
        // En victoria o derrota la tragaperras ya se ha reiniciado: esos marcadores se actualizan al volver a esta pantalla
        if (resultado.estado == EstadoPartida.EN_CURSO) actualizarMarcadores(animarSaldo = true)
        // En victoria o derrota los botones se activan cuando llega la partida nueva de la BD
        if (partida != null) activarJuego(true)

        when (resultado.estado) {
            EstadoPartida.VICTORIA -> abrirVictoria(resultado, boteAntes)
            EstadoPartida.DERROTA -> efectoPantalla(R.color.red, sacudir = true, intensidad = 0.5f) { abrirDerrota(resultado, boteAntes) }
            // Una tirada normal en la que se pierden monedas también "duele", pero más suave
            EstadoPartida.EN_CURSO -> if (resultado.variacion < 0) efectoPantalla(R.color.red, sacudir = true, intensidad = 0.3f)
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
                    if (!animador.girando) activarJuego(true)
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
    private fun actualizarMarcadores(animarSaldo: Boolean = false) {
        findViewById<TextView>(R.id.tvBote).text = formatear(tragaperras.bote)
        findViewById<TextView>(R.id.tvApuesta).text = tragaperras.apuesta.toString()

        val saldo = findViewById<TextView>(R.id.tvSaldo)
        contador?.cancel()
        if (animarSaldo && saldoMostrado != tragaperras.saldo) {
            contarSaldo(saldo, saldoMostrado, tragaperras.saldo)
            // La moneda gira cuando termina de contar el saldo
            girarMoneda(findViewById(R.id.ivMonedaSaldo), RETRASO_SEGUNDO_MENSAJE + DURACION_CUENTA)
        } else {
            saldo.text = formatear(tragaperras.saldo)
        }
        saldoMostrado = tragaperras.saldo
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

        // Animaciones del resultado: primero el panel 1 y, un momento después, el panel 2
        aparecer(findViewById(R.id.panelMensaje))
        aparecer(findViewById(R.id.panelResultado), RETRASO_SEGUNDO_MENSAJE)
    }

    // Deja los carteles ocultos, pero con el mensaje de bienvenida listo para cuando se vuelva a esta pantalla
    private fun prepararBienvenida() {
        findViewById<TextView>(R.id.tvMensaje).setText(R.string.mensaje_bienvenida)
        findViewById<TextView>(R.id.tvResultadoTitulo).setText(R.string.mensaje_pulsa_girar)
        findViewById<TextView>(R.id.tvResultado).text = ""
        findViewById<View>(R.id.ivMonedaResultado).visibility = View.INVISIBLE
        bienvenidaPendiente = true
    }

    // Al volver a esta pantalla, los carteles de bienvenida aparecen con el mismo "pop"
    private fun mostrarBienvenida() {
        if (!bienvenidaPendiente) return
        bienvenidaPendiente = false
        aparecer(findViewById(R.id.panelMensaje))
        aparecer(findViewById(R.id.panelResultado), RETRASO_SEGUNDO_MENSAJE)
    }

    // Mientras giran los rodillos, los dos paneles se desvanecen
    private fun ocultarMensajes() {
        listOf(R.id.panelMensaje, R.id.panelResultado).forEach { id ->
            findViewById<View>(id).animate().cancel()
            findViewById<View>(id).animate()
                .alpha(0f).setStartDelay(0).setDuration(150).setInterpolator(null).start()
        }
    }

    // El panel aparece creciendo desde pequeño, con un pequeño rebote, tras [retraso] ms
    private fun aparecer(vista: View, retraso: Long = 0) {
        vista.animate().cancel()
        vista.scaleX = 0.6f
        vista.scaleY = 0.6f
        vista.alpha = 0f
        vista.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setStartDelay(retraso).setDuration(300)
            .setInterpolator(OvershootInterpolator(2f))
            .start()
    }

    // La moneda da una vuelta completa sobre su eje vertical, tras [retraso] ms
    private fun girarMoneda(moneda: View, retraso: Long) {
        moneda.animate().cancel()
        moneda.rotationY = 0f
        moneda.animate().rotationY(360f).setStartDelay(retraso).setDuration(500).start()
    }

    // El saldo cuenta desde [desde] hasta [hasta]: en verde si sube, en rojo si baja
    private fun contarSaldo(vista: TextView, desde: Int, hasta: Int) {
        vista.text = formatear(desde)
        vista.setTextColor(ContextCompat.getColor(this, if (hasta > desde) R.color.green else R.color.red))
        contador = ValueAnimator.ofInt(desde, hasta).apply {
            startDelay = RETRASO_SEGUNDO_MENSAJE
            duration = DURACION_CUENTA
            addUpdateListener { vista.text = formatear(it.animatedValue as Int) }
            doOnEnd {
                vista.text = formatear(hasta)
                vista.setTextColor(ContextCompat.getColor(this@GameActivity, R.color.yellow))
            }
            start()
        }
    }

    // Destello de color sobre toda la pantalla (y sacudida si se pierde). Al acabar ejecuta [alTerminar]
    private fun efectoPantalla(@ColorRes color: Int, sacudir: Boolean, intensidad: Float, alTerminar: () -> Unit = {}) {
        val contenido = findViewById<ViewGroup>(android.R.id.content)
        val destello = View(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@GameActivity, color))
            alpha = 0f
            // Se "come" los toques: mientras dura el efecto no se puede pulsar nada de lo que hay debajo
            isClickable = true
        }
        contenido.addView(
            destello,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        val animaciones = mutableListOf<Animator>(
            ObjectAnimator.ofFloat(destello, View.ALPHA, 0f, intensidad, 0f).apply { duration = 700 }
        )
        if (sacudir) {
            val dp = resources.displayMetrics.density
            val principal = findViewById<View>(R.id.main)
            animaciones.add(
                ObjectAnimator.ofFloat(
                    principal, View.TRANSLATION_X,
                    0f, -12 * dp, 12 * dp, -8 * dp, 8 * dp, -4 * dp, 4 * dp, 0f
                ).apply { duration = 500 }
            )
        }

        AnimatorSet().apply {
            playTogether(animaciones)
            doOnEnd {
                contenido.removeView(destello)
                // Si el usuario ha salido de la pantalla mientras tanto, se abre cuando vuelva
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) alTerminar() else pendiente = alTerminar
            }
            start()
        }
    }

    private fun abrirVictoria(resultado: ResultadoTirada, boteAntes: Int) {
        val intent = Intent(this, VictoryActivity::class.java)
            .putExtra(EXTRA_BOTE_BASE, boteAntes)
            .putExtra(EXTRA_MULTIPLICADOR, resultado.variacion / boteAntes)
            .putExtra(EXTRA_PREMIO, resultado.variacion)
            .putExtra(EXTRA_SALDO, resultado.saldoDespues)
            .putExtra(EXTRA_SALDO_ANTES, resultado.saldoDespues - resultado.variacion)
        startActivity(intent)
    }

    private fun abrirDerrota(resultado: ResultadoTirada, boteAntes: Int) {
        val intent = Intent(this, DefeatActivity::class.java)
            .putExtra(EXTRA_BOTE_BASE, boteAntes)
            .putExtra(EXTRA_NUEVO_BOTE, tragaperras.bote)
            .putExtra(EXTRA_SALDO, resultado.saldoDespues)
            .putExtra(EXTRA_SALDO_ANTES, resultado.saldoDespues - resultado.variacion)
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
        const val EXTRA_SALDO_ANTES = "saldo_antes"

        /** 1300 -> "1.300" */
        fun formatear(monedas: Int): String = String.format(Locale.GERMANY, "%,d", monedas)

        /** 600 -> "+600", -150 -> "-150" */
        fun formatearConSigno(monedas: Int): String = String.format(Locale.GERMANY, "%+,d", monedas)

        /** Fecha guardada en la BD (epoch millis) -> "19/10/2026" */
        fun formatearFecha(millis: Long): String =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))

        // Cuánto después del primer mensaje salen el segundo y el saldo
        private const val RETRASO_SEGUNDO_MENSAJE = 300L

        // Cuánto tarda el saldo en contar hasta su nuevo valor
        private const val DURACION_CUENTA = 600L
    }
}
