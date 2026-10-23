package com.bugbusters.randomspace.game

import com.bugbusters.randomspace.model.EstadoPartida
import com.bugbusters.randomspace.model.Simbolo

data class ResultadoTirada(
    val rodillos: List<Simbolo>,
    val combinacion: Combinacion,
    val apuesta: Int,
    val variacion: Int,    // monedas ganadas  o perdidas (+/-)
    val saldoDespues: Int, // saldo justo tras la tirada antes de reiniciar
    val estado: EstadoPartida
)