package com.bugbusters.randomspace.data.repository

import com.bugbusters.randomspace.data.local.RandomSpaceDao
import com.bugbusters.randomspace.model.Bote
import com.bugbusters.randomspace.model.EstadoPartida
import com.bugbusters.randomspace.model.Jugador
import com.bugbusters.randomspace.model.Partida
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

/** Lo que muestra la pantalla de inicio, cargado de una vez. */
data class DatosInicio(val jugador: Jugador, val bote: Bote, val mejoresPartidas: List<Partida>)

/** Lo que necesita la pantalla de juego para arrancar la tragaperras. */
data class DatosPartida(val partida: Partida, val bote: Bote)

/**
 * Puerta de entrada a los datos para la UI. Envuelve el DAO (síncrono) en tipos de RxJava
 * y decide en qué hilo se ejecuta cada operación:
 * - lecturas en Schedulers.io(): varias pueden ir a la vez;
 * - escrituras en Schedulers.single(): un único hilo, así se guardan en el orden en que se piden.
 *
 * La UI solo tiene que añadir observeOn(AndroidSchedulers.mainThread()) antes de pintar.
 * Los valores iniciales (saldo, bote...) llegan como parámetros: data/ no importa game/ReglasJuego.
 */
class RandomSpaceRepository(private val dao: RandomSpaceDao) {

    private val lecturas = Schedulers.io()
    private val escrituras = Schedulers.single()

    // ---------- Jugador ----------

    fun existeJugador(): Single<Boolean> =
        Single.fromCallable { dao.existeJugador() }
            .subscribeOn(lecturas)

    fun registrarJugador(
        nombre: String, saldoInicial: Int, boteInicial: Int, incrementoBote: Int, apuestaInicial: Int
    ): Single<Long> =
        Single.fromCallable {
            dao.registrarJugador(nombre, saldoInicial, boteInicial, incrementoBote, apuestaInicial)
        }.subscribeOn(escrituras)

    /** Vacío (sin error) si aún no hay jugador. */
    fun obtenerJugador(): Maybe<Jugador> =
        Maybe.fromCallable<Jugador> { dao.obtenerJugador() }
            .subscribeOn(lecturas)

    // ---------- Pantalla de inicio ----------

    /**
     * Jugador, bote activo y mejores partidas en una sola respuesta.
     * Primero el jugador (los otros dos necesitan su id); después bote y top se piden a la vez con zip.
     * Vacío si aún no hay jugador.
     */
    fun obtenerDatosInicio(limiteTop: Int): Maybe<DatosInicio> =
        obtenerJugador().flatMapSingle { jugador ->
            Single.zip(
                obtenerBoteActivo(jugador.id).toSingle(),
                obtenerMejoresPartidas(jugador.id, limiteTop)
            ) { bote, mejores -> DatosInicio(jugador, bote, mejores) }
        }

    fun obtenerBoteActivo(jugadorId: Long): Maybe<Bote> =
        Maybe.fromCallable<Bote> { dao.obtenerBoteActivo(jugadorId) }
            .subscribeOn(lecturas)

    fun obtenerMejoresPartidas(jugadorId: Long, limite: Int): Single<List<Partida>> =
        Single.fromCallable { dao.obtenerMejoresPartidas(jugadorId, limite) }
            .subscribeOn(lecturas)

    // ---------- Pantalla de juego ----------

    /**
     * Partida EN_CURSO del jugador (o una nueva si no tiene) y el bote activo.
     * Va en escrituras porque puede insertar la partida.
     */
    fun cargarPartida(saldoInicial: Int): Single<DatosPartida> =
        Single.fromCallable {
            val jugador = checkNotNull(dao.obtenerJugador()) { "No hay jugador registrado" }
            val bote = checkNotNull(dao.obtenerBoteActivo(jugador.id)) { "El jugador no tiene bote" }
            val partida = dao.obtenerPartidaEnCurso(jugador.id) ?: run {
                val nueva = Partida(
                    jugadorId = jugador.id,
                    boteId = bote.id,
                    resultado = saldoInicial,
                    saldoInicial = saldoInicial,
                    saldoFinal = saldoInicial
                )
                nueva.copy(id = dao.insertarPartida(nueva))
            }
            DatosPartida(partida, bote)
        }.subscribeOn(escrituras)

    /**
     * Calcula cómo queda la partida tras una tirada. No toca la BD (es inmediato):
     * la pantalla actualiza su copia al momento y luego la guarda con [guardarTirada].
     */
    fun aplicarTirada(partida: Partida, saldo: Int): Partida =
        partida.copy(saldoFinal = saldo, resultado = maxOf(partida.resultado, saldo))

    /** Guarda la partida (ya calculada con [aplicarTirada]) y sincroniza el saldo del jugador. */
    fun guardarTirada(partida: Partida): Completable =
        Completable.fromAction {
            dao.guardarTirada(partida.id, partida.jugadorId, partida.saldoFinal, partida.resultado)
        }.subscribeOn(escrituras)

    /**
     * Cierra la partida como DERROTA (el bote crece) y empieza otra.
     */
    fun terminarConDerrota(partida: Partida, saldoFinal: Int, saldoInicial: Int): Single<Partida> =
        Single.fromCallable {
            dao.terminarConDerrota(cerrar(partida, EstadoPartida.DERROTA, saldoFinal), saldoInicial)
        }.subscribeOn(escrituras)

    /**
     * Cierra la partida como VICTORIA (se crea un bote nuevo) y empieza otra.
     */
    fun terminarConVictoria(
        partida: Partida, saldoFinal: Int, saldoInicial: Int, boteInicial: Int, incrementoBote: Int
    ): Single<Partida> =
        Single.fromCallable {
            dao.terminarConVictoria(
                cerrar(partida, EstadoPartida.VICTORIA, saldoFinal), saldoInicial, boteInicial, incrementoBote
            )
        }.subscribeOn(escrituras)

    // ---------- Historial ----------

    fun obtenerHistorial(jugadorId: Long): Single<List<Partida>> =
        Single.fromCallable { dao.obtenerHistorial(jugadorId) }
            .subscribeOn(lecturas)

    private fun cerrar(partida: Partida, estado: EstadoPartida, saldoFinal: Int): Partida =
        aplicarTirada(partida, saldoFinal).copy(estado = estado)
}
