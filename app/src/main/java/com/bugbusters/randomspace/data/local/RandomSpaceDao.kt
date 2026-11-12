package com.bugbusters.randomspace.data.local

import android.content.ContentValues
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import com.bugbusters.randomspace.data.local.RandomSpaceContract.TablaBote
import com.bugbusters.randomspace.data.local.RandomSpaceContract.TablaJugador
import com.bugbusters.randomspace.data.local.RandomSpaceContract.TablaPartida
import com.bugbusters.randomspace.data.local.RandomSpaceContract.TablaTragaperras
import com.bugbusters.randomspace.model.Bote
import com.bugbusters.randomspace.model.EstadoPartida
import com.bugbusters.randomspace.model.Jugador
import com.bugbusters.randomspace.model.Partida


//Operaciones sobre SQLite: convierte filas en objetos de model/ y objetos en filas.
//Los métodos son síncronos (acceden a disco): el repositorio decide en qué hilo se ejecutan.
class RandomSpaceDao(private val dbHelper: RandomSpaceDbHelper) {

    // ---------- Jugador ----------

    //true si ya hay un jugador registrado en el dispositivo.
    fun existeJugador(): Boolean =
        DatabaseUtils.queryNumEntries(dbHelper.readableDatabase, TablaJugador.NOMBRE_TABLA) > 0

    //El jugador del dispositivo (solo hay uno), o null si aún no se ha registrado.
    fun obtenerJugador(): Jugador? {
        dbHelper.readableDatabase.query(
            TablaJugador.NOMBRE_TABLA, null, null, null, null, null, null, "1"
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.aJugador() else null
        }
    }

    fun registrarJugador(
        nombre: String, saldoInicial: Int, boteInicial: Int, incrementoBote: Int, apuestaInicial: Int
    ): Long = enTransaccion { db ->
        val jugadorId = db.insertOrThrow(TablaJugador.NOMBRE_TABLA, null, ContentValues().apply {
            put(TablaJugador.COL_NOMBRE, nombre)
            put(TablaJugador.COL_SALDO, saldoInicial)
            put(TablaJugador.COL_PUNTUACION_MAX, 0)
        })
        val boteId = insertarBote(db, boteInicial, incrementoBote)
        db.insertOrThrow(TablaTragaperras.NOMBRE_TABLA, null, ContentValues().apply {
            put(TablaTragaperras.COL_JUGADOR_ID, jugadorId)
            put(TablaTragaperras.COL_BOTE_ID, boteId)
            put(TablaTragaperras.COL_APUESTA_ACTUAL, apuestaInicial)
        })
        jugadorId
    }

    // ---------- Bote ----------

    //Bote en juego: el que apunta la tragaperras del jugador.
    fun obtenerBoteActivo(jugadorId: Long): Bote? {
        val sql = """
            SELECT b.* FROM ${TablaBote.NOMBRE_TABLA} b
            INNER JOIN ${TablaTragaperras.NOMBRE_TABLA} t
                ON t.${TablaTragaperras.COL_BOTE_ID} = b.${TablaBote.COL_ID}
            WHERE t.${TablaTragaperras.COL_JUGADOR_ID} = ?
        """.trimIndent()
        dbHelper.readableDatabase.rawQuery(sql, arrayOf(jugadorId.toString())).use { cursor ->
            return if (cursor.moveToFirst()) cursor.aBote() else null
        }
    }

    // ---------- Partida ----------

    //La partida EN_CURSO del jugador, o null si no tiene ninguna.
    fun obtenerPartidaEnCurso(jugadorId: Long): Partida? {
        dbHelper.readableDatabase.query(
            TablaPartida.NOMBRE_TABLA, null,
            "${TablaPartida.COL_JUGADOR_ID} = ? AND ${TablaPartida.COL_ESTADO} = ?",
            arrayOf(jugadorId.toString(), EstadoPartida.EN_CURSO.name),
            null, null, null, "1"
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.aPartida() else null
        }
    }

    //Inserta una partida. Solo hace falta para la primera: las siguientes las crean
    //[terminarConDerrota] y [terminarConVictoria].
    fun insertarPartida(partida: Partida): Long = insertarPartida(dbHelper.writableDatabase, partida)

    //Guarda el saldo tras una tirada en la partida en curso y en el jugador, a la vez.
    fun guardarTirada(partidaId: Long, jugadorId: Long, saldo: Int, resultado: Int) {
        enTransaccion { db ->
            db.update(TablaPartida.NOMBRE_TABLA, ContentValues().apply {
                put(TablaPartida.COL_SALDO_FINAL, saldo)
                put(TablaPartida.COL_RESULTADO, resultado)
            }, "${TablaPartida.COL_ID} = ?", arrayOf(partidaId.toString()))
            actualizarSaldoJugador(db, jugadorId, saldo)
        }
    }


    //Cierra la partida como DERROTA: el bote crece con su incremento y empieza
    //una partida nueva con el mismo bote.

    fun terminarConDerrota(partida: Partida, saldoInicial: Int): Partida = enTransaccion { db ->
        cerrarPartida(db, partida)
        db.execSQL(
            "UPDATE ${TablaBote.NOMBRE_TABLA}" +
                " SET ${TablaBote.COL_CANTIDAD} = ${TablaBote.COL_CANTIDAD} + ${TablaBote.COL_INCREMENTO}" +
                " WHERE ${TablaBote.COL_ID} = ?",
            arrayOf(partida.boteId)
        )
        actualizarJugadorTrasPartida(db, partida.jugadorId, saldoInicial, partida.resultado)
        empezarPartida(db, partida.jugadorId, partida.boteId, saldoInicial)
    }


    //Cierra la partida como VICTORIA: el bote ganado se queda como registro, se crea uno nuevo,
    //la tragaperras pasa a apuntar a él y empieza una partida nueva.
    fun terminarConVictoria(
        partida: Partida, saldoInicial: Int, boteInicial: Int, incrementoBote: Int
    ): Partida = enTransaccion { db ->
        cerrarPartida(db, partida)
        val nuevoBoteId = insertarBote(db, boteInicial, incrementoBote)
        db.update(TablaTragaperras.NOMBRE_TABLA, ContentValues().apply {
            put(TablaTragaperras.COL_BOTE_ID, nuevoBoteId)
        }, "${TablaTragaperras.COL_JUGADOR_ID} = ?", arrayOf(partida.jugadorId.toString()))
        actualizarJugadorTrasPartida(db, partida.jugadorId, saldoInicial, partida.resultado)
        empezarPartida(db, partida.jugadorId, nuevoBoteId, saldoInicial)
    }

    // Solo las victorias, de mayor a menor saldo final
    fun obtenerMejoresPartidas(jugadorId: Long, limite: Int): List<Partida> = consultarPartidasTerminadas(
        jugadorId, "${TablaPartida.COL_SALDO_FINAL} DESC, ${TablaPartida.COL_FECHA} DESC", limite.toString(),
        soloVictorias = true
    )

    fun obtenerHistorial(jugadorId: Long): List<Partida> =
        consultarPartidasTerminadas(jugadorId, "${TablaPartida.COL_FECHA} DESC", null)

    // ---------- Ayudantes privados ----------

    private inline fun <T> enTransaccion(bloque: (SQLiteDatabase) -> T): T {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val resultado = bloque(db)
            db.setTransactionSuccessful()
            return resultado
        } finally {
            db.endTransaction()
        }
    }

    private fun insertarBote(db: SQLiteDatabase, cantidad: Int, incremento: Int): Long =
        db.insertOrThrow(TablaBote.NOMBRE_TABLA, null, ContentValues().apply {
            put(TablaBote.COL_CANTIDAD, cantidad)
            put(TablaBote.COL_INCREMENTO, incremento)
        })

    private fun insertarPartida(db: SQLiteDatabase, partida: Partida): Long =
        db.insertOrThrow(TablaPartida.NOMBRE_TABLA, null, ContentValues().apply {
            put(TablaPartida.COL_JUGADOR_ID, partida.jugadorId)
            put(TablaPartida.COL_BOTE_ID, partida.boteId)
            put(TablaPartida.COL_FECHA, partida.fecha)
            put(TablaPartida.COL_RESULTADO, partida.resultado)
            put(TablaPartida.COL_SALDO_INICIAL, partida.saldoInicial)
            put(TablaPartida.COL_SALDO_FINAL, partida.saldoFinal)
            put(TablaPartida.COL_ESTADO, partida.estado.name)
        })

    private fun empezarPartida(db: SQLiteDatabase, jugadorId: Long, boteId: Long, saldoInicial: Int): Partida {
        val nueva = Partida(
            jugadorId = jugadorId,
            boteId = boteId,
            resultado = saldoInicial,
            saldoInicial = saldoInicial,
            saldoFinal = saldoInicial
        )
        return nueva.copy(id = insertarPartida(db, nueva))
    }

    private fun cerrarPartida(db: SQLiteDatabase, partida: Partida) {
        require(partida.estado != EstadoPartida.EN_CURSO) { "La partida a cerrar debe estar terminada" }
        db.update(TablaPartida.NOMBRE_TABLA, ContentValues().apply {
            put(TablaPartida.COL_SALDO_FINAL, partida.saldoFinal)
            put(TablaPartida.COL_RESULTADO, partida.resultado)
            put(TablaPartida.COL_ESTADO, partida.estado.name)
        }, "${TablaPartida.COL_ID} = ?", arrayOf(partida.id.toString()))
    }

    private fun actualizarSaldoJugador(db: SQLiteDatabase, jugadorId: Long, saldo: Int) {
        db.update(TablaJugador.NOMBRE_TABLA, ContentValues().apply {
            put(TablaJugador.COL_SALDO, saldo)
        }, "${TablaJugador.COL_ID} = ?", arrayOf(jugadorId.toString()))
    }

    private fun actualizarJugadorTrasPartida(db: SQLiteDatabase, jugadorId: Long, saldo: Int, resultado: Int) {
        db.execSQL(
            "UPDATE ${TablaJugador.NOMBRE_TABLA}" +
                " SET ${TablaJugador.COL_SALDO} = ?," +
                " ${TablaJugador.COL_PUNTUACION_MAX} = MAX(${TablaJugador.COL_PUNTUACION_MAX}, ?)" +
                " WHERE ${TablaJugador.COL_ID} = ?",
            arrayOf(saldo, resultado, jugadorId)
        )
    }

    private fun consultarPartidasTerminadas(
        jugadorId: Long, orden: String, limite: String?, soloVictorias: Boolean = false
    ): List<Partida> {
        val condicionEstado = if (soloVictorias) "=" else "!="
        val estado = if (soloVictorias) EstadoPartida.VICTORIA else EstadoPartida.EN_CURSO
        dbHelper.readableDatabase.query(
            TablaPartida.NOMBRE_TABLA, null,
            "${TablaPartida.COL_JUGADOR_ID} = ? AND ${TablaPartida.COL_ESTADO} $condicionEstado ?",
            arrayOf(jugadorId.toString(), estado.name),
            null, null, orden, limite
        ).use { cursor ->
            val partidas = ArrayList<Partida>(cursor.count)
            while (cursor.moveToNext()) partidas.add(cursor.aPartida())
            return partidas
        }
    }

    // ---------- Fila del Cursor -> objeto del modelo ----------

    private fun Cursor.aJugador() = Jugador(
        id = getLong(getColumnIndexOrThrow(TablaJugador.COL_ID)),
        nombre = getString(getColumnIndexOrThrow(TablaJugador.COL_NOMBRE)),
        saldo = getInt(getColumnIndexOrThrow(TablaJugador.COL_SALDO)),
        puntuacionMaxima = getInt(getColumnIndexOrThrow(TablaJugador.COL_PUNTUACION_MAX))
    )

    private fun Cursor.aBote() = Bote(
        id = getLong(getColumnIndexOrThrow(TablaBote.COL_ID)),
        cantidad = getInt(getColumnIndexOrThrow(TablaBote.COL_CANTIDAD)),
        incremento = getInt(getColumnIndexOrThrow(TablaBote.COL_INCREMENTO))
    )

    private fun Cursor.aPartida() = Partida(
        id = getLong(getColumnIndexOrThrow(TablaPartida.COL_ID)),
        jugadorId = getLong(getColumnIndexOrThrow(TablaPartida.COL_JUGADOR_ID)),
        boteId = getLong(getColumnIndexOrThrow(TablaPartida.COL_BOTE_ID)),
        fecha = getLong(getColumnIndexOrThrow(TablaPartida.COL_FECHA)),
        resultado = getInt(getColumnIndexOrThrow(TablaPartida.COL_RESULTADO)),
        saldoInicial = getInt(getColumnIndexOrThrow(TablaPartida.COL_SALDO_INICIAL)),
        saldoFinal = getInt(getColumnIndexOrThrow(TablaPartida.COL_SALDO_FINAL)),
        estado = EstadoPartida.valueOf(getString(getColumnIndexOrThrow(TablaPartida.COL_ESTADO)))
    )
}
