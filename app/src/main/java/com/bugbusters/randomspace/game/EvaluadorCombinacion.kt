package com.bugbusters.randomspace.game

import com.bugbusters.randomspace.model.Simbolo

object EvaluadorCombinacion {

    fun evaluar(rodillos: List<Simbolo>): Combinacion {
        require(rodillos.size == 3) { "Tiene que haber 3 rodillos" }

        // Cuántas veces sale cada símbolo
        val cuenta = rodillos.groupingBy { it }.eachCount()
        fun veces(simbolo: Simbolo) = cuenta[simbolo] ?: 0
        val todosPlanetas = rodillos.all { it.esPlaneta }

        return when {
            veces(Simbolo.HAMBURGUESA) == 3 -> Combinacion.PREMIO_GORDO
            veces(Simbolo.OVNI) == 3 -> Combinacion.INVASION_ALIENIGENA
            veces(Simbolo.SOL) == 3 -> Combinacion.NOS_QUEMAMOS
            veces(Simbolo.ESTRELLA) == 3 -> Combinacion.LLUVIA_DE_ESTRELLAS
            todosPlanetas && cuenta.size == 3 -> Combinacion.ALINEACION
            todosPlanetas && cuenta.size == 1 -> Combinacion.TRIO_PLANETARIO
            veces(Simbolo.HAMBURGUESA) == 2 -> Combinacion.APERITIVO
            veces(Simbolo.OVNI) == 2 -> Combinacion.NOS_PERSIGUEN
            veces(Simbolo.SOL) == 2 -> Combinacion.QUE_CALOR
            cuenta.values.any { it == 2 } -> Combinacion.DOBLE
            veces(Simbolo.OVNI) == 1 -> Combinacion.NOS_ATACAN
            else -> Combinacion.SIGUE_EXPLORANDO
        }
    }
}