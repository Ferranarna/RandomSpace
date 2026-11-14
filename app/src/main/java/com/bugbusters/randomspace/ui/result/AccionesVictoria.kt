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
 * Punto único de lo que ocurre al ganar. VictoryActivity lo crea con la pantalla y lo llama una sola vez al abrirse.
 * Cada funcionalidad vive en SU PROPIO archivo y aquí solo se añade su creación y una línea con su llamada,
 * para que nadie tenga que editar a la vez el mismo archivo.
 */
class AccionesVictoria(activity: AppCompatActivity) {

    // Cada acción se crea aquí, con la pantalla, para poder registrar sus permisos antes de que arranque
    private val calendario = CalendarioVictoria(activity)
    // A: private val notificacion = NotificacionVictoria(activity)
    // C: private val ubicacion = UbicacionVictoria(activity)

    fun alGanar(datos: DatosVictoria) {
        calendario.guardar(datos)
        // A: notificacion.mostrar(datos)
        // C: ubicacion.guardar(datos)
    }
}