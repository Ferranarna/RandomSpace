package com.bugbusters.randomspace.game

import com.bugbusters.randomspace.model.Simbolo.*
import org.junit.Assert.assertEquals
import org.junit.Test

class EvaluadorCombinacionTest {

    private fun evaluar(vararg simbolos: com.bugbusters.randomspace.model.Simbolo) =
        EvaluadorCombinacion.evaluar(simbolos.toList())

    @Test
    fun tripleHamburguesaEsPremioGordo() {
        assertEquals(Combinacion.PREMIO_GORDO, evaluar(HAMBURGUESA, HAMBURGUESA, HAMBURGUESA))
    }

    @Test
    fun tresPlanetasDistintosEsAlineacion() {
        assertEquals(Combinacion.ALINEACION, evaluar(LUNA, TIERRA, JUPITER))
    }

    @Test
    fun tresPlanetasIgualesEsTrioPlanetario() {
        assertEquals(Combinacion.TRIO_PLANETARIO, evaluar(TIERRA, TIERRA, TIERRA))
    }

    @Test
    fun dobleHamburguesaEsAperitivo() {
        assertEquals(Combinacion.APERITIVO, evaluar(HAMBURGUESA, ESTRELLA, HAMBURGUESA))
    }

    @Test
    fun tripleEstrellaEsLluviaDeEstrellas() {
        assertEquals(Combinacion.LLUVIA_DE_ESTRELLAS, evaluar(ESTRELLA, ESTRELLA, ESTRELLA))
    }

    @Test
    fun dosPlanetasIgualesEsDoble() {
        assertEquals(Combinacion.DOBLE, evaluar(LUNA, LUNA, SOL))
    }

    @Test
    fun tripleOvniYTripleSol() {
        assertEquals(Combinacion.INVASION_ALIENIGENA, evaluar(OVNI, OVNI, OVNI))
        assertEquals(Combinacion.NOS_QUEMAMOS, evaluar(SOL, SOL, SOL))
    }

    @Test
    fun dobleOvniYDobleSol() {
        assertEquals(Combinacion.NOS_PERSIGUEN, evaluar(OVNI, ESTRELLA, OVNI))
        assertEquals(Combinacion.QUE_CALOR, evaluar(SOL, SOL, ESTRELLA))
    }

    @Test
    fun unOvniEsNosAtacan() {
        assertEquals(Combinacion.NOS_ATACAN, evaluar(OVNI, ESTRELLA, SOL))
    }

    @Test
    fun sinCombinacionEsSigueExplorando() {
        assertEquals(Combinacion.SIGUE_EXPLORANDO, evaluar(HAMBURGUESA, ESTRELLA, SOL))
    }
}
