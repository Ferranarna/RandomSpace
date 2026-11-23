package com.bugbusters.randomspace.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.bugbusters.randomspace.R

/** Efectos de sonido de los rodillos: un sonido en bucle mientras giran y un golpe cuando para cada uno. */
class SonidosRodillos(context: Context) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val giro = pool.load(context, R.raw.sonido_giro, 1)
    private val parada = pool.load(context, R.raw.sonido_parada, 1)
    private var sonandoGiro = 0

    /** Empieza el sonido de giro en bucle (-1 = repetir hasta que se pare). */
    fun empezarGiro() {
        sonandoGiro = pool.play(giro, 0.6f, 0.6f, 1, -1, 1f)
    }

    /** Golpe al parar el rodillo [indice]: cada uno un poco más agudo que el anterior. */
    fun pararRodillo(indice: Int) {
        pool.play(parada, 1f, 1f, 2, 0, 1f + indice * 0.1f)
    }

    /** Corta el sonido de giro. */
    fun terminar() {
        if (sonandoGiro != 0) pool.stop(sonandoGiro)
        sonandoGiro = 0
    }

    /** Libera la memoria de los sonidos al cerrar la pantalla. */
    fun liberar() = pool.release()
}