package com.bugbusters.randomspace.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import com.bugbusters.randomspace.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Cómo es una lluvia de partículas: de qué color, cuántas, a qué velocidad, si giran...
 * Las medidas van en dp (y dp por segundo), para que se vea igual en cualquier pantalla.
 */
class EstiloParticulas(
    val colores: List<Int>,      // recursos de color (R.color.xxx)
    val numPiezas: Int,
    val velocidadMin: Float,     // dp por segundo, hacia abajo
    val velocidadMax: Float,
    val tamanoMin: Float,        // dp
    val tamanoMax: Float,
    val balanceoMin: Float,      // dp de vaivén lateral
    val balanceoMax: Float,
    val deriva: Float,           // dp por segundo hacia un lado (viento): positivo = derecha, negativo = izquierda
    val retrasoMaximo: Float,    // segundos durante los que van empezando
    val vida: Float,             // segundos que vive cada pieza: pasado ese tiempo se desvanece
    val gira: Boolean,           // si se estrechan y ensanchan como dando vueltas
    val encoge: Boolean,         // si se van haciendo más pequeñas a lo largo de su vida
    val empiezaArriba: Boolean,  // true: entran por arriba; false: ya están en el aire y aparecen poco a poco
    val alfa: Int                // 0 (transparente) a 255 (opaco)
) {
    companion object {
        /** Victoria: colores vivos, piezas grandes y girando. */
        val CONFETI = EstiloParticulas(
            colores = listOf(R.color.yellow, R.color.green, R.color.cyan, R.color.magenta, R.color.white),
            numPiezas = 90, velocidadMin = 300f, velocidadMax = 500f,
            tamanoMin = 8f, tamanoMax = 16f, balanceoMin = 10f, balanceoMax = 30f,
            deriva = 0f, retrasoMaximo = 0.8f, vida = 10f,
            gira = true, encoge = false, empiezaArriba = true, alfa = 255
        )

        /** Derrota: polvo fino que flota, empujado por el viento, y se deshace. */
        val CENIZA = EstiloParticulas(
            colores = listOf(R.color.ash_gray, R.color.ash_dark),
            numPiezas = 140, velocidadMin = 50f, velocidadMax = 110f,
            tamanoMin = 3f, tamanoMax = 6f, balanceoMin = 8f, balanceoMax = 20f,
            deriva = 45f, retrasoMaximo = 0.6f, vida = 3.5f,
            gira = false, encoge = true, empiezaArriba = false, alfa = 255
        )
    }
}

/**
 * Lluvia de cuadraditos de píxel que cae por toda la pantalla (confeti, cenizas...) según su [EstiloParticulas].
 * Se dibuja con Canvas: en cada fotograma se calcula dónde está cada pieza y se pinta un rectángulo.
 * No recibe toques: los deja pasar a lo que haya debajo. Al acabar, se quita sola de la pantalla.
 */
class ParticulasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // Una pieza. Todo se decide al empezar; en cada fotograma solo se calcula su posición
    private class Pieza(
        val x0: Float,        // posición horizontal de partida (px)
        val y0: Float,        // altura de partida (px)
        val retraso: Float,   // segundos que espera antes de empezar
        val velocidad: Float, // px por segundo, hacia abajo
        val tamano: Float,    // lado del cuadrado (px)
        val balanceo: Float,  // amplitud del vaivén lateral (px)
        val deriva: Float,    // px por segundo hacia un lado (viento)
        val fase: Float,      // desfase, para que no vayan todas sincronizadas
        val color: Int
    )

    private val piezas = ArrayList<Pieza>()
    private val pincel = Paint().apply {
        isAntiAlias = false   // bordes nítidos, como píxeles
        style = Paint.Style.FILL
    }
    private var estilo = EstiloParticulas.CONFETI
    private var tiempo = 0f   // segundos desde que empezó
    private var animador: ValueAnimator? = null

    /** Empieza la lluvia. Hay que llamarlo cuando la vista ya está añadida a la pantalla. */
    fun lanzar(estilo: EstiloParticulas) {
        this.estilo = estilo
        doOnLayout {
            val segundos = crearPiezas()
            animador = ValueAnimator.ofFloat(0f, segundos).apply {
                duration = (segundos * 1000).toLong()
                interpolator = LinearInterpolator()
                addUpdateListener {
                    tiempo = it.animatedValue as Float
                    invalidate() // pide que se vuelva a dibujar
                }
                doOnEnd { (parent as? ViewGroup)?.removeView(this@ParticulasView) }
                start()
            }
        }
    }

    // Reparte las piezas por la pantalla y devuelve cuántos segundos dura todo
    private fun crearPiezas(): Float {
        val dp = resources.displayMetrics.density
        val colores = estilo.colores.map { ContextCompat.getColor(context, it) }
        // El viento empuja las piezas hacia un lado: las que empiezan fuera de la pantalla entran con él
        val margen = abs(estilo.deriva) * dp * estilo.vida
        val desde = if (estilo.deriva > 0f) -margen else 0f
        val hasta = width + (if (estilo.deriva < 0f) margen else 0f)
        piezas.clear()
        repeat(estilo.numPiezas) {
            val tamano = azar(estilo.tamanoMin, estilo.tamanoMax) * dp
            piezas.add(
                Pieza(
                    x0 = azar(desde, hasta),
                    y0 = if (estilo.empiezaArriba) -tamano else azar(0f, height.toFloat()),
                    retraso = azar(0f, estilo.retrasoMaximo),
                    velocidad = azar(estilo.velocidadMin, estilo.velocidadMax) * dp,
                    tamano = tamano,
                    balanceo = azar(estilo.balanceoMin, estilo.balanceoMax) * dp,
                    deriva = estilo.deriva * dp * azar(0.8f, 1.2f),
                    fase = azar(0f, 6.28f),
                    color = colores[Random.nextInt(colores.size)]
                )
            )
        }
        // Lo que dura cada pieza es lo menor entre lo que tarda en salir por abajo y su vida
        return piezas.maxOf { it.retraso + minOf((height + it.tamano - it.y0) / it.velocidad, estilo.vida) }
    }

    private fun azar(minimo: Float, maximo: Float): Float = minimo + Random.nextFloat() * (maximo - minimo)

    override fun onDraw(canvas: Canvas) {
        for (p in piezas) {
            val t = tiempo - p.retraso
            if (t < 0f) continue                    // todavía no le toca
            val restante = estilo.vida - t
            if (restante <= 0f) continue            // ya ha desaparecido
            val y = p.y0 + t * p.velocidad
            if (y > height) continue                // ya ha salido por abajo
            val x = p.x0 + p.deriva * t + sin(t * 3f + p.fase) * p.balanceo
            // Si se encoge, se va haciendo más pequeña (hasta un 40 %) a lo largo de su vida
            val lado = if (estilo.encoge) p.tamano * (1f - 0.6f * (t / estilo.vida)) else p.tamano
            // Si gira, el ancho se estrecha y se ensancha, como una pieza dando vueltas
            val ancho = if (estilo.gira) lado * abs(cos(t * 6f + p.fase)) else lado
            // Si no nace arriba, aparece poco a poco; al final de su vida se desvanece
            val entrada = if (estilo.empiezaArriba) 1f else minOf(1f, t / FUNDIDO)
            val salida = if (restante < FUNDIDO) restante / FUNDIDO else 1f
            pincel.color = p.color
            pincel.alpha = (estilo.alfa * entrada * salida).toInt()  // el alfa va después del color, que lo reinicia
            canvas.drawRect(x - ancho / 2, y, x + ancho / 2, y + lado, pincel)
        }
    }

    // Si la pantalla se cierra a mitad, se para la animación
    override fun onDetachedFromWindow() {
        animador?.cancel()
        super.onDetachedFromWindow()
    }

    private companion object {
        const val FUNDIDO = 0.6f   // segundos que tarda una pieza en aparecer o desvanecerse
    }
}