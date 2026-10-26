package com.bugbusters.randomspace.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.bugbusters.randomspace.data.local.RandomSpaceContract.TablaJugador
import com.bugbusters.randomspace.data.local.RandomSpaceContract.TablaBote
import com.bugbusters.randomspace.data.local.RandomSpaceContract.TablaPartida
import com.bugbusters.randomspace.data.local.RandomSpaceContract.TablaTragaperras

class RandomSpaceDbHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    RandomSpaceContract.NOMBRE_BD,
    null,
    RandomSpaceContract.VERSION_BD
) {

    // Se ejecuta cada vez que se abre la BD
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // Solo la primera vez: cuando el archivo randomspace.db no existe
    override fun onCreate(db: SQLiteDatabase) {
        val crearJugador = """
            CREATE TABLE ${TablaJugador.NOMBRE_TABLA} (
                ${TablaJugador.COL_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TablaJugador.COL_NOMBRE} TEXT NOT NULL,
                ${TablaJugador.COL_SALDO} INTEGER NOT NULL,
                ${TablaJugador.COL_PUNTUACION_MAX} INTEGER NOT NULL
            )
        """.trimIndent()
        val crearBote = """
            CREATE TABLE ${TablaBote.NOMBRE_TABLA} (
                ${TablaBote.COL_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TablaBote.COL_CANTIDAD} INTEGER NOT NULL,
                ${TablaBote.COL_INCREMENTO} INTEGER NOT NULL
            )
        """.trimIndent()
        val crearTragaperras = """
            CREATE TABLE ${TablaTragaperras.NOMBRE_TABLA} (
                ${TablaTragaperras.COL_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TablaTragaperras.COL_BOTE_ID} INTEGER UNIQUE NOT NULL,
                ${TablaTragaperras.COL_JUGADOR_ID} INTEGER UNIQUE NOT NULL,
                ${TablaTragaperras.COL_APUESTA_ACTUAL} INTEGER NOT NULL,
                ${TablaTragaperras.COL_SIMBOLO_1} TEXT,
                ${TablaTragaperras.COL_SIMBOLO_2} TEXT,
                ${TablaTragaperras.COL_SIMBOLO_3} TEXT,
                    FOREIGN KEY (${TablaTragaperras.COL_BOTE_ID})
                        REFERENCES ${TablaBote.NOMBRE_TABLA} (${TablaBote.COL_ID}),
                    FOREIGN KEY (${TablaTragaperras.COL_JUGADOR_ID})
                        REFERENCES ${TablaJugador.NOMBRE_TABLA} (${TablaJugador.COL_ID})
            )
        """.trimIndent()
        val crearPartida = """
            CREATE TABLE ${TablaPartida.NOMBRE_TABLA} (
                ${TablaPartida.COL_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TablaPartida.COL_JUGADOR_ID} INTEGER NOT NULL,
                ${TablaPartida.COL_BOTE_ID} INTEGER NOT NULL,
                ${TablaPartida.COL_FECHA} INTEGER NOT NULL,
                ${TablaPartida.COL_RESULTADO} INTEGER NOT NULL,
                ${TablaPartida.COL_ESTADO} TEXT NOT NULL,
                ${TablaPartida.COL_SALDO_INICIAL} INTEGER NOT NULL,
                ${TablaPartida.COL_SALDO_FINAL} INTEGER NOT NULL,
                    CHECK (${TablaPartida.COL_ESTADO} IN ('EN_CURSO', 'VICTORIA', 'DERROTA')),
                    FOREIGN KEY (${TablaPartida.COL_BOTE_ID})
                        REFERENCES ${TablaBote.NOMBRE_TABLA} (${TablaBote.COL_ID}),
                    FOREIGN KEY (${TablaPartida.COL_JUGADOR_ID})
                        REFERENCES ${TablaJugador.NOMBRE_TABLA} (${TablaJugador.COL_ID})
            )
        """.trimIndent()

        db.execSQL(crearJugador)
        db.execSQL(crearBote)
        db.execSQL(crearTragaperras)
        db.execSQL(crearPartida)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${TablaPartida.NOMBRE_TABLA}")
        db.execSQL("DROP TABLE IF EXISTS ${TablaTragaperras.NOMBRE_TABLA}")
        db.execSQL("DROP TABLE IF EXISTS ${TablaBote.NOMBRE_TABLA}")
        db.execSQL("DROP TABLE IF EXISTS ${TablaJugador.NOMBRE_TABLA}")
        onCreate(db)
    }
}