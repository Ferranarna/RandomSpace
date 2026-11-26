package com.bugbusters.randomspace.model

/**
 * Jugador que ha iniciado sesión con Google.
 * [uid] es su identificador en Firebase: no cambia aunque cambie de móvil.
 * [nombreGoogle] es el nombre de su cuenta; para el top 10 se usa el nombre que elige en el juego.
 */
data class Sesion(
    val uid: String,
    val nombreGoogle: String
)