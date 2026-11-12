package com.bugbusters.randomspace.game

object ReglasJuego {
    const val SALDO_INICIAL = 1000
    const val BOTE_INICIAL = 1000
    const val INCREMENTO_BOTE = 100

    const val APUESTA_MINIMA = 10
    const val PASO_APUESTA = 10
    // PENDIENTE: no tengo claro si quedamos en 30 o 100 la apuesta maxima.
    const val APUESTA_MAXIMA = 100
    // PENDIENTE: true = bote × (apuesta / 10), false = bote entero (tampoco tenia claro)
    const val BOTE_SE_MULTIPLICA = true

    const val NUM_RODILLOS = 3
}