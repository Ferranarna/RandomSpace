package com.bugbusters.randomspace.ui

import android.animation.ValueAnimator
import android.view.View
import android.widget.TextView
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import com.bugbusters.randomspace.R

/** Animaciones de números, compartidas por varias pantallas. */
object AnimacionNumeros {

    private const val DURACION_MONEDA = 350L

    /**
     * Hace que el número de [vista] cuente desde [desde] hasta [hasta], tras [retraso] ms.
     * Mientras cuenta se pinta en verde si sube o en rojo si baja, y al acabar recupera su color.
     * Al terminar la cuenta, [moneda] (si se indica) da una vuelta, y después se ejecuta [alTerminar].
     * [formato] convierte el número en el texto que se ve (por ejemplo, 1300 -> "1.300").
     * Devuelve la animación para poder cancelarla al cerrar la pantalla (si se cancela, no se avisa).
     */
    fun contar(
        vista: TextView, desde: Int, hasta: Int,
        retraso: Long = 300, duracion: Long = 600,
        moneda: View? = null,
        alTerminar: () -> Unit = {},
        formato: (Int) -> String
    ): ValueAnimator {
        val colorOriginal = vista.currentTextColor
        vista.text = formato(desde) // hasta que empieza la cuenta se ve el valor de partida
        var cancelada = false
        return ValueAnimator.ofInt(desde, hasta).apply {
            startDelay = retraso
            duration = duracion
            doOnStart {
                if (hasta != desde) {
                    val color = if (hasta > desde) R.color.green else R.color.red
                    vista.setTextColor(ContextCompat.getColor(vista.context, color))
                }
            }
            addUpdateListener { vista.text = formato(it.animatedValue as Int) }
            doOnCancel { cancelada = true }
            doOnEnd {
                vista.text = formato(hasta)
                vista.setTextColor(colorOriginal)
                if (cancelada) return@doOnEnd
                if (moneda != null && hasta != desde) girarMoneda(moneda, alTerminar) else alTerminar()
            }
            start()
        }
    }

    // La moneda da una vuelta completa sobre su eje vertical y, al acabar, ejecuta [alTerminar]
    private fun girarMoneda(moneda: View, alTerminar: () -> Unit) {
        moneda.animate().cancel()
        moneda.rotationY = 0f
        moneda.animate()
            .rotationY(360f).setStartDelay(0).setDuration(DURACION_MONEDA)
            .withEndAction { alTerminar() }
            .start()
    }
}