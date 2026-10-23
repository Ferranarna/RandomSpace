// model/Partida.kt
package com.bugbusters.randomspace.model

data class Partida(
    val id: Long = 0,
    val jugadorId: Long,
    val boteId: Long,
    val fecha: Long = System.currentTimeMillis(),
    val resultado: Int,
    val saldoInicial: Int,
    val saldoFinal: Int,
    val estado: EstadoPartida = EstadoPartida.EN_CURSO
)