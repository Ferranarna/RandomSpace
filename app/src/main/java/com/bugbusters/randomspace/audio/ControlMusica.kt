package com.bugbusters.randomspace.audio

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.edit

/**
 * Preferencias de la música (activada o no, y qué canción) y arranque/parada del servicio.
 * Se guarda en SharedPreferences para que se recuerde al cerrar la app.
 */
object ControlMusica {

    private const val PREFERENCIAS = "musica"
    private const val CLAVE_ACTIVADA = "activada"
    private const val CLAVE_CANCION = "cancion"

    private fun preferencias(context: Context) =
        context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)

    /** true si el jugador quiere música (por defecto sí). */
    fun estaActivada(context: Context) = preferencias(context).getBoolean(CLAVE_ACTIVADA, true)

    /** Canción elegida del móvil, o null si se usa la melodía oficial. */
    fun cancionElegida(context: Context): Uri? =
        preferencias(context).getString(CLAVE_CANCION, null)?.let(Uri::parse)

    /** Botón del menú: si sonaba la para, y si no sonaba la arranca. */
    fun alternar(context: Context) {
        val activada = !estaActivada(context)
        preferencias(context).edit { putBoolean(CLAVE_ACTIVADA, activada) }
        if (activada) arrancar(context) else parar(context)
    }

    /** Guarda la canción elegida (null = oficial) y, si la música está activada, la pone. */
    fun elegirCancion(context: Context, uri: Uri?) {
        guardarCancion(context, uri)
        arrancar(context)
    }

    /** Solo guarda la canción, sin tocar el servicio (lo usa el servicio si la canción ya no existe). */
    fun guardarCancion(context: Context, uri: Uri?) {
        preferencias(context).edit { putString(CLAVE_CANCION, uri?.toString()) }
    }

    /** Arranca el servicio si la música está activada. Si ya está en marcha, solo le avisa. */
    fun arrancar(context: Context) {
        if (estaActivada(context)) {
            context.startService(Intent(context.applicationContext, MusicaService::class.java))
        }
    }

    fun parar(context: Context) {
        context.stopService(Intent(context.applicationContext, MusicaService::class.java))
    }
}