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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Pequeña explosión de estrellas de píxel que salen disparadas desde un punto, frenan y se apagan.
 * Se dibuja con Canvas. No recibe toques y, al acabar, se quita sola de la pantalla.
 */
class EstrellasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // Una estrella. Todo se decide al empezar; en cada fotograma solo se calcula su posición
    private class Estrella(
        val dx: Float,       // dirección horizontal (coseno del ángulo)
        val dy: Float,       // dirección vertical (seno del ángulo)
        val alcance: Float,  // hasta dónde llega (px)
        val tamano: Float,   // lado del cuadradito central (px)
        val fase: Float,     // desfase del parpadeo
        val color: Int
    )

    private val estrellas = ArrayList<Estrella>()
    private val pincel = Paint().apply {
        isAntiAlias = false   // bordes nítidos, como píxeles
        style = Paint.Style.FILL
    }
    private var progreso = 0f   // de 0 (empieza) a 1 (acaba)
    private var origenX = 0f
    private var origenY = 0f
    private var animador: ValueAnimator? = null

    /** Explota desde el centro de [ancla]. Hay que llamarlo cuando la vista ya está añadida a la pantalla. */
    fun lanzar(ancla: View) {
        doOnLayout {
            // Centro del ancla, medido respecto a esta vista (que ocupa toda la pantalla)
            val posAncla = IntArray(2)
            val posYo = IntArray(2)
            ancla.getLocationInWindow(posAncla)
            getLocationInWindow(posYo)
            origenX = posAncla[0] - posYo[0] + ancla.width / 2f
            origenY = posAncla[1] - posYo[1] + ancla.height / 2f

            crearEstrellas()
            animador = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = DURACION
                interpolator = LinearInterpolator()
                addUpdateListener {
                    progreso = it.animatedValue as Float
                    invalidate() // pide que se vuelva a dibujar
                }
                doOnEnd { (parent as? ViewGroup)?.removeView(this@EstrellasView) }
                start()
            }
        }
    }

    private fun crearEstrellas() {
        val dp = resources.displayMetrics.density
        val blanco = ContextCompat.getColor(context, R.color.white)
        val amarillo = ContextCompat.getColor(context, R.color.yellow)
        estrellas.clear()
        repeat(NUM_ESTRELLAS) {
            val angulo = azar(0f, 6.28f)
            estrellas.add(
                Estrella(
                    dx = cos(angulo),
                    dy = sin(angulo),
                    alcance = azar(110f, 300f) * dp,
                    tamano = azar(3f, 6f) * dp,
                    fase = azar(0f, 6.28f),
                    color = if (Random.nextFloat() < 0.3f) blanco else amarillo
                )
            )
        }
    }

    private fun azar(minimo: Float, maximo: Float): Float = minimo + Random.nextFloat() * (maximo - minimo)

    override fun onDraw(canvas: Canvas) {
        // Sale disparada y va frenando: avanza mucho al principio y poco al final
        val resto = 1f - progreso
        val avance = 1f - resto * resto * resto
        // Se mantienen brillantes casi todo el recorrido y se apagan solo en el último tramo
        val opacidad = minOf(1f, resto / 0.35f)
        for (e in estrellas) {
            val x = origenX + e.dx * e.alcance * avance
            val y = origenY + e.dy * e.alcance * avance
            // Parpadea mientras viaja
            val brillo = 0.85f + 0.15f * sin(progreso * 20f + e.fase)
            pincel.color = e.color
            pincel.alpha = (255 * opacidad * brillo).toInt()  // el alfa va después del color, que lo reinicia
            val s = e.tamano
            // Estrella de píxel: una cruz formada por dos rectángulos
            canvas.drawRect(x - 1.5f * s, y - s / 2, x + 1.5f * s, y + s / 2, pincel)
            canvas.drawRect(x - s / 2, y - 1.5f * s, x + s / 2, y + 1.5f * s, pincel)
        }
    }

    // Si la pantalla se cierra a mitad, se para la animación
    override fun onDetachedFromWindow() {
        animador?.cancel()
        super.onDetachedFromWindow()
    }

    private companion object {
        const val NUM_ESTRELLAS = 22
        const val DURACION = 1100L   // ms que dura la explosión
    }
}