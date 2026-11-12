// model/Jugador.kt
package com.bugbusters.randomspace.model

data class Jugador(
    val id: Long = 0,
    val nombre: String,
    val saldo: Int = 1000,
    val puntuacionMaxima: Int = 0
)