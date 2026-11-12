package com.bugbusters.randomspace.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator

/** Animaciones de vistas, compartidas por varias pantallas. */
object AnimacionVistas {

    /** La vista aparece creciendo desde pequeña, con un pequeño rebote, tras [retraso] ms. */
    fun aparecer(vista: View, retraso: Long = 0) {
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

    /**
     * Las [vistas] caen juntas desde arriba, como un bloque, y rebotan en su sitio.
     */
    // Ajustes de la caída de la cabecera
    private const val DISTANCIA_CAIDA_DP = 180   // desde cuántos dp por encima empieza
    private const val DURACION_CAIDA = 800L      // ms que dura todo (con rebote)
    private const val DURACION_FUNDIDO = 250L    // ms que tarda en hacerse visible

    private const val RETRASO_CAIDA = 250L   // ms de pausa antes de que empiece a caer

    /**
     * Las [vistas] caen juntas desde arriba, como un bloque, y rebotan al llegar a su sitio.
     * Mientras bajan se van haciendo visibles.
     */
    fun caer(vistas: List<View>, retraso: Long = RETRASO_CAIDA) {
        val distancia = -DISTANCIA_CAIDA_DP * vistas.first().resources.displayMetrics.density
        vistas.forEach { vista ->
            // Antes de empezar quedan arriba y transparentes
            vista.translationY = distancia
            vista.alpha = 0f

            val caida = ObjectAnimator.ofFloat(vista, View.TRANSLATION_Y, distancia, 0f).apply {
                duration = DURACION_CAIDA
                interpolator = BounceInterpolator()
            }
            val fundido = ObjectAnimator.ofFloat(vista, View.ALPHA, 0f, 1f).apply { duration = DURACION_FUNDIDO }
            AnimatorSet().apply {
                playTogether(caida, fundido)
                startDelay = retraso
                start()
            }
        }
    }
}