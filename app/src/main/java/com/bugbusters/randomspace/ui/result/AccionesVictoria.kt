package com.bugbusters.randomspace.ui.result

import androidx.appcompat.app.AppCompatActivity

/** Datos de una victoria que necesitan las acciones que se lanzan al ganar. */
data class DatosVictoria(
    val fecha: Long,         // momento de la victoria (epoch millis)
    val boteBase: Int,       // bote antes de multiplicar
    val multiplicador: Int,  // apuesta / 10 (de 1 a 10)
    val premio: Int,         // boteBase x multiplicador
    val saldoFinal: Int      // saldo tras cobrar el premio
)

/**
 * Punto único de lo que ocurre al ganar. VictoryActivity lo llama una sola vez al abrirse.
 * Cada funcionalidad vive en SU PROPIO archivo y aquí solo se añade una línea con su llamada,
 * para que nadie tenga que editar a la vez el mismo archivo.
 */
object AccionesVictoria {

    fun alGanar(activity: AppCompatActivity, datos: DatosVictoria) {
        // Añade aquí tu línea cuando tu archivo esté listo, por ejemplo:
        // NotificacionVictoria.mostrar(activity, datos)    // A: notificación
        // CalendarioVictoria.guardar(activity, datos)      // B: evento en el calendario
        // UbicacionVictoria.guardar(activity, datos)       // C: ubicación en SQLite
    }
}