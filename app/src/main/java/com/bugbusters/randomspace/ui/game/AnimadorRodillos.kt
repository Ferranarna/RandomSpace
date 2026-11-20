package com.bugbusters.randomspace.ui.game

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import com.bugbusters.randomspace.model.Simbolo

interface EscuchaGiro {
    /** Los rodillos empiezan a girar. */
    fun alEmpezar() {}

    /** El rodillo [indice] (0, 1 o 2) acaba de parar. */
    fun alPararRodillo(indice: Int) {}

    /** Los tres rodillos han parado. */
    fun alTerminar() {}
}

/**
 * Anima los tres rodillos: cada uno es una tira vertical de símbolos que sube dentro de su ventana
 * (el FrameLayout que contiene su ImageView), frena y rebota un poco al parar.
 * No decide ningún resultado: recibe los símbolos que ya han salido y solo los enseña.
 *
 * @param imagenes los tres ImageView de los rodillos; cada uno debe estar dentro de un FrameLayout
 */
class AnimadorRodillos(private val imagenes: List<ImageView>) {

    /** true mientras hay una animación en marcha. */
    var girando = false
        private set

    private val ventanas = imagenes.map { it.parent as FrameLayout }
    private val ultimos = MutableList(imagenes.size) { Simbolo.HAMBURGUESA }
    private val tiras = arrayOfNulls<LinearLayout>(imagenes.size)
    private val animadores = mutableListOf<Animator>()
    private var destinos: List<Simbolo> = emptyList()
    private var restantes = 0
    private var cancelado = false

    /** Pone los símbolos en reposo, sin animar (para el estado inicial). */
    fun mostrar(simbolos: List<Simbolo>) {
        simbolos.forEachIndexed { i, simbolo ->
            ultimos[i] = simbolo
            imagenes[i].setImageResource(simbolo.imagen())
        }
    }

    /** Hace girar los rodillos hasta dejarlos en [destinos]. Si ya están girando, no hace nada. */
    fun girar(destinos: List<Simbolo>, escucha: EscuchaGiro) {
        if (girando) return
        girando = true
        cancelado = false
        this.destinos = destinos
        restantes = imagenes.size
        escucha.alEmpezar()

        imagenes.indices.forEach { i ->
            val tira = construirTira(i, destinos[i])
            tiras[i] = tira
            imagenes[i].visibility = View.INVISIBLE

            val alto = ventanas[i].height.toFloat()
            val fin = -(CELDAS_ALEATORIAS + 1) * alto   // posición en la que se ve el símbolo real
            val pasarse = alto * REBOTE                  // cuánto se pasa antes de volver

            val giro = ObjectAnimator.ofFloat(tira, View.TRANSLATION_Y, 0f, fin - pasarse).apply {
                duration = DURACION_BASE + i * DURACION_EXTRA
                interpolator = DecelerateInterpolator(1.5f)
                doOnEnd { if (!cancelado) escucha.alPararRodillo(i) }
            }
            val asentar = ObjectAnimator.ofFloat(tira, View.TRANSLATION_Y, fin - pasarse, fin).apply {
                duration = DURACION_REBOTE
                interpolator = OvershootInterpolator(2f)
            }
            val conjunto = AnimatorSet().apply {
                playSequentially(giro, asentar)
                doOnEnd {
                    if (cancelado) return@doOnEnd
                    dejarEnReposo(i)
                    restantes--
                    if (restantes == 0) {
                        girando = false
                        animadores.clear()
                        escucha.alTerminar()
                    }
                }
            }
            animadores.add(conjunto)
            conjunto.start()
        }
    }

    /**
     * Corta la animación (al salir de la pantalla) y deja los rodillos en su símbolo final.
     * No llama a [EscuchaGiro.alTerminar].
     */
    fun cancelar() {
        if (!girando) return
        cancelado = true
        animadores.forEach { it.cancel() }
        animadores.clear()
        imagenes.indices.forEach { dejarEnReposo(it) }
        girando = false
    }

    // Celda 0: el símbolo anterior · 1..N: al azar · N+1: el resultado · última: extra para el rebote
    private fun construirTira(i: Int, destino: Simbolo): LinearLayout {
        val ventana = ventanas[i]
        val alto = ventana.height
        val simbolos = buildList {
            add(ultimos[i])
            repeat(CELDAS_ALEATORIAS) { add(Simbolo.entries.random()) }
            add(destino)
            add(Simbolo.entries.random())
        }
        val tira = LinearLayout(ventana.context).apply { orientation = LinearLayout.VERTICAL }
        simbolos.forEach { simbolo ->
            val celda = ImageView(ventana.context).apply { setImageResource(simbolo.imagen()) }
            tira.addView(celda, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, alto))
        }
        ventana.addView(tira, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, alto * simbolos.size))
        return tira
    }

    // Quita la tira y deja el ImageView con el símbolo final
    private fun dejarEnReposo(i: Int) {
        tiras[i]?.let { ventanas[i].removeView(it) }
        tiras[i] = null
        ultimos[i] = destinos[i]
        imagenes[i].setImageResource(destinos[i].imagen())
        imagenes[i].visibility = View.VISIBLE
    }

    private companion object {
        const val CELDAS_ALEATORIAS = 10
        const val DURACION_BASE = 900L    // ms que gira el primer rodillo
        const val DURACION_EXTRA = 350L   // ms de más por cada rodillo siguiente
        const val DURACION_REBOTE = 250L  // ms del rebote final
        const val REBOTE = 0.2f           // fracción de la altura que se pasa antes de volver
    }
}