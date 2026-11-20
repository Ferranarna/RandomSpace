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
 * Confeti de píxel que cae por toda la pantalla durante un par de segundos.
 * Se dibuja con Canvas: en cada fotograma se calcula dónde está cada pieza y se pinta un rectángulo.
 * No recibe toques: los deja pasar a lo que haya debajo. Al acabar, se quita solo de la pantalla.
 */
class ConfetiView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // Una pieza de confeti. Todo se decide al empezar; en cada fotograma solo se calcula su posición
    private class Pieza(
        val x0: Float,        // posición horizontal de partida (px)
        val retraso: Float,   // segundos que espera antes de empezar a caer
        val velocidad: Float, // px por segundo
        val tamano: Float,    // lado del cuadrado (px)
        val balanceo: Float,  // amplitud del vaivén lateral (px)
        val fase: Float,      // desfase, para que no vayan todas sincronizadas
        val color: Int
    )

    private val piezas = ArrayList<Pieza>()
    private val pincel = Paint().apply {
        isAntiAlias = false   // bordes nítidos, como píxeles
        style = Paint.Style.FILL
    }
    private var tiempo = 0f   // segundos desde que empezó
    private var animador: ValueAnimator? = null

    /** Empieza a caer el confeti. Hay que llamarlo cuando la vista ya está añadida a la pantalla. */
    fun lanzar() {
        doOnLayout {
            val segundos = crearPiezas()
            animador = ValueAnimator.ofFloat(0f, segundos).apply {
                duration = (segundos * 1000).toLong()
                interpolator = LinearInterpolator()
                addUpdateListener {
                    tiempo = it.animatedValue as Float
                    invalidate() // pide que se vuelva a dibujar
                }
                doOnEnd { (parent as? ViewGroup)?.removeView(this@ConfetiView) }
                start()
            }
        }
    }

    // Reparte las piezas por el ancho de la pantalla y devuelve cuántos segundos dura todo
    private fun crearPiezas(): Float {
        val dp = resources.displayMetrics.density
        val colores = intArrayOf(
            ContextCompat.getColor(context, R.color.yellow),
            ContextCompat.getColor(context, R.color.green),
            ContextCompat.getColor(context, R.color.cyan),
            ContextCompat.getColor(context, R.color.magenta),
            ContextCompat.getColor(context, R.color.white)
        )
        piezas.clear()
        repeat(NUM_PIEZAS) {
            piezas.add(
                Pieza(
                    x0 = azar(0f, width.toFloat()),
                    retraso = azar(0f, RETRASO_MAXIMO),
                    velocidad = azar(300f, 500f) * dp,
                    tamano = azar(8f, 16f) * dp,
                    balanceo = azar(10f, 30f) * dp,
                    fase = azar(0f, 6.28f),
                    color = colores[Random.nextInt(colores.size)]
                )
            )
        }
        // Lo que tarda la última pieza en salir por abajo
        return piezas.maxOf { it.retraso + (height + it.tamano) / it.velocidad }
    }

    private fun azar(minimo: Float, maximo: Float): Float = minimo + Random.nextFloat() * (maximo - minimo)

    override fun onDraw(canvas: Canvas) {
        for (p in piezas) {
            val t = tiempo - p.retraso
            if (t < 0f) continue                    // todavía no le toca
            val y = -p.tamano + t * p.velocidad
            if (y > height) continue                // ya ha salido por abajo
            val x = p.x0 + sin(t * 3f + p.fase) * p.balanceo
            // El ancho se estrecha y se ensancha, como si la pieza diera vueltas
            val ancho = p.tamano * abs(cos(t * 6f + p.fase))
            pincel.color = p.color
            canvas.drawRect(x - ancho / 2, y, x + ancho / 2, y + p.tamano, pincel)
        }
    }

    // Si la pantalla se cierra a mitad, se para la animación
    override fun onDetachedFromWindow() {
        animador?.cancel()
        super.onDetachedFromWindow()
    }

    private companion object {
        const val NUM_PIEZAS = 90
        const val RETRASO_MAXIMO = 0.8f   // segundos: las piezas empiezan a caer repartidas en este tiempo
    }
}