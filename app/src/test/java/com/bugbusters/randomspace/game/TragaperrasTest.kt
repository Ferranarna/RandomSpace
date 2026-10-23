package com.bugbusters.randomspace.game

import com.bugbusters.randomspace.model.EstadoPartida
import com.bugbusters.randomspace.model.Simbolo
import com.bugbusters.randomspace.model.Simbolo.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TragaperrasTest {

    // Generador "trucado": devuelve siempre los símbolos que le pasemos, en orden
    private fun generadorFijo(vararg simbolos: Simbolo): GeneradorSimbolos {
        var i = 0
        return GeneradorSimbolos { simbolos[i++ % simbolos.size] }
    }

    @Test
    fun laApuestaSubeYBajaDeDiezEnDiez() {
        val maquina = Tragaperras()
        assertTrue(maquina.aumentarApuesta())
        assertEquals(20, maquina.apuesta)
        assertTrue(maquina.disminuirApuesta())
        assertEquals(10, maquina.apuesta)
    }

    @Test
    fun laApuestaNoBajaDelMinimo() {
        val maquina = Tragaperras()
        assertFalse(maquina.disminuirApuesta())
        assertEquals(ReglasJuego.APUESTA_MINIMA, maquina.apuesta)
    }

    @Test
    fun laApuestaNoSuperaElMaximo() {
        val maquina = Tragaperras()
        repeat(50) { maquina.aumentarApuesta() }
        assertEquals(ReglasJuego.APUESTA_MAXIMA, maquina.apuesta)
    }

    @Test
    fun laApuestaNoSuperaElSaldo() {
        val maquina = Tragaperras(saldoInicial = 30)
        repeat(10) { maquina.aumentarApuesta() }
        assertEquals(30, maquina.apuesta)
    }

    @Test
    fun elPremioSeMultiplicaPorLaApuesta() {
        val maquina = Tragaperras(generador = generadorFijo(HAMBURGUESA, HAMBURGUESA, ESTRELLA))
        maquina.aumentarApuesta()
        maquina.aumentarApuesta() // apuesta 30 -> x3

        val resultado = maquina.girar()

        assertEquals(Combinacion.APERITIVO, resultado.combinacion)
        assertEquals(600, resultado.variacion) // 200 x 3
        assertEquals(1600, maquina.saldo)
        assertEquals(EstadoPartida.EN_CURSO, resultado.estado)
    }

    @Test
    fun sinCombinacionElSaldoNoCambia() {
        val maquina = Tragaperras(generador = generadorFijo(HAMBURGUESA, ESTRELLA, SOL))
        val resultado = maquina.girar()
        assertEquals(0, resultado.variacion)
        assertEquals(1000, maquina.saldo)
    }

    @Test
    fun elSaldoNuncaBajaDeCeroYEsDerrota() {
        val maquina = Tragaperras(saldoInicial = 80, generador = generadorFijo(SOL, SOL, SOL))
        maquina.aumentarApuesta()
        maquina.aumentarApuesta() // apuesta 30 -> -600

        val resultado = maquina.girar()

        assertEquals(0, resultado.saldoDespues)
        assertEquals(-80, resultado.variacion)
        assertEquals(EstadoPartida.DERROTA, resultado.estado)
    }

    @Test
    fun laDerrotaReiniciaElSaldoYSubeElBote() {
        val maquina = Tragaperras(saldoInicial = 40, generador = generadorFijo(OVNI, ESTRELLA, SOL))
        maquina.girar() // 40 - 50 -> 0

        assertEquals(ReglasJuego.SALDO_INICIAL, maquina.saldo)
        assertEquals(ReglasJuego.BOTE_INICIAL + ReglasJuego.INCREMENTO_BOTE, maquina.bote)
    }

    @Test
    fun tripleHamburguesaEsVictoriaYReinicia() {
        val maquina = Tragaperras(boteInicial = 1500, generador = generadorFijo(HAMBURGUESA))
        maquina.aumentarApuesta() // apuesta 20 -> x2

        val resultado = maquina.girar()

        val premioEsperado = if (ReglasJuego.BOTE_SE_MULTIPLICA) 1500 * 2 else 1500
        assertEquals(EstadoPartida.VICTORIA, resultado.estado)
        assertEquals(premioEsperado, resultado.variacion)
        assertEquals(1000 + premioEsperado, resultado.saldoDespues)
        assertEquals(ReglasJuego.SALDO_INICIAL, maquina.saldo)
        assertEquals(ReglasJuego.BOTE_INICIAL, maquina.bote)
    }

    @Test
    fun siElSaldoBajaDeLaApuestaLaApuestaSeAjusta() {
        val maquina = Tragaperras(saldoInicial = 400, generador = generadorFijo(OVNI, ESTRELLA, SOL))
        repeat(6) { maquina.aumentarApuesta() } // apuesta 70 -> x7

        maquina.girar() // -50 x 7 = -350 -> saldo 50

        assertEquals(50, maquina.saldo)
        assertEquals(50, maquina.apuesta)
    }
}
