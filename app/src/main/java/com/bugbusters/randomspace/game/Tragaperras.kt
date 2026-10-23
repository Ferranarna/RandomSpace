package com.bugbusters.randomspace.game

import com.bugbusters.randomspace.model.EstadoPartida

class Tragaperras(
    saldoInicial: Int = ReglasJuego.SALDO_INICIAL,
    boteInicial: Int = ReglasJuego.BOTE_INICIAL,
    private val generador: GeneradorSimbolos = GeneradorSimbolosAleatorio()
) {
    var saldo = saldoInicial
        private set
    var bote = boteInicial
        private set
    var apuesta = ReglasJuego.APUESTA_MINIMA
        private set

    fun puedeGirar(): Boolean = saldo >= apuesta

    fun aumentarApuesta(): Boolean {
        val nueva = apuesta + ReglasJuego.PASO_APUESTA
        if (nueva > ReglasJuego.APUESTA_MAXIMA || nueva > saldo) return false
        apuesta = nueva
        return true
    }

    fun disminuirApuesta(): Boolean {
        val nueva = apuesta - ReglasJuego.PASO_APUESTA
        if (nueva < ReglasJuego.APUESTA_MINIMA) return false
        apuesta = nueva
        return true
    }

    fun girar(): ResultadoTirada {
        check(puedeGirar()) { "Saldo insuficiente para apostar $apuesta" }

        val rodillos = List(ReglasJuego.NUM_RODILLOS) { generador.siguiente() }
        val combinacion = EvaluadorCombinacion.evaluar(rodillos)
        val multiplicador = apuesta / ReglasJuego.APUESTA_MINIMA

        val premio = if (combinacion == Combinacion.PREMIO_GORDO) {
            if (ReglasJuego.BOTE_SE_MULTIPLICA) bote * multiplicador else bote
        } else {
            combinacion.valorBase * multiplicador
        }

        val saldoDespues = maxOf(0, saldo + premio) // el saldo nunca baja de 0
        val estado = when {
            combinacion == Combinacion.PREMIO_GORDO -> EstadoPartida.VICTORIA
            saldoDespues < ReglasJuego.APUESTA_MINIMA -> EstadoPartida.DERROTA
            else -> EstadoPartida.EN_CURSO
        }
        val resultado = ResultadoTirada(
            rodillos, combinacion, apuesta, saldoDespues - saldo, saldoDespues, estado
        )

        when (estado) {
            EstadoPartida.VICTORIA -> {
                saldo = ReglasJuego.SALDO_INICIAL
                bote = ReglasJuego.BOTE_INICIAL
            }
            EstadoPartida.DERROTA -> {
                saldo = ReglasJuego.SALDO_INICIAL
                bote += ReglasJuego.INCREMENTO_BOTE
            }
            EstadoPartida.EN_CURSO -> saldo = saldoDespues
        }
        ajustarApuesta()
        return resultado
    }

    // Si tras perder el saldo ya no cubre la apuesta, la baja al maximo que se pueda pagar
    private fun ajustarApuesta() {
        if (apuesta > saldo) {
            apuesta = maxOf(ReglasJuego.APUESTA_MINIMA, saldo / ReglasJuego.PASO_APUESTA * ReglasJuego.PASO_APUESTA)
        }
    }
}