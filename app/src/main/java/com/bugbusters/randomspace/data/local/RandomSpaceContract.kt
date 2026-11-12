package com.bugbusters.randomspace.data.local

object RandomSpaceContract {
    const val NOMBRE_BD = "randomspace.db"
    const val VERSION_BD = 1

    object TablaJugador {
        const val NOMBRE_TABLA = "jugador"
        const val COL_ID = "id"
        const val COL_NOMBRE = "nombre"
        const val COL_SALDO = "saldo"
        const val COL_PUNTUACION_MAX = "puntuacion_maxima"
    }

    object TablaBote {
        const val NOMBRE_TABLA = "bote"
        const val COL_ID = "id"
        const val COL_CANTIDAD = "cantidad"
        const val COL_INCREMENTO = "incremento"
    }

    object TablaTragaperras {
        const val NOMBRE_TABLA = "tragaperras"
        const val COL_ID = "id"
        const val COL_JUGADOR_ID = "jugador_id"
        const val COL_BOTE_ID = "bote_id"
        const val COL_APUESTA_ACTUAL = "apuesta_actual"
        const val COL_SIMBOLO_1 = "simbolo_1"
        const val COL_SIMBOLO_2 = "simbolo_2"
        const val COL_SIMBOLO_3 = "simbolo_3"
    }

    object TablaPartida {
        const val NOMBRE_TABLA = "partida"
        const val COL_ID = "id"
        const val COL_JUGADOR_ID = "jugador_id"
        const val COL_BOTE_ID = "bote_id"
        const val COL_FECHA = "fecha"
        const val COL_RESULTADO = "resultado"
        const val COL_SALDO_INICIAL = "saldo_inicial"
        const val COL_SALDO_FINAL = "saldo_final"
        const val COL_ESTADO = "estado"
    }
}