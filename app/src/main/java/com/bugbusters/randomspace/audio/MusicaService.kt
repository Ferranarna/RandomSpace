package com.bugbusters.randomspace.audio

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.bugbusters.randomspace.R

/**
 * Servicio que toca la música de fondo en bucle.
 * - prepareAsync: el archivo se prepara en segundo plano, sin bloquear la pantalla.
 * - Foco de audio: si entra una llamada, una alarma u otra app pone sonido, la música se pausa
 *   y vuelve cuando el móvil nos devuelve el audio.
 */
class MusicaService : Service(), AudioManager.OnAudioFocusChangeListener {

    private lateinit var audioManager: AudioManager
    private var reproductor: MediaPlayer? = null
    private var fuenteActual: Uri? = null       // null = melodía oficial
    private var preparado = false
    private var pausadaPorFoco = false          // la pausó una llamada o alarma: hay que volver sola
    private var peticionFoco: AudioFocusRequest? = null

    // No se conecta ninguna pantalla al servicio: solo se arranca y se para
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
    }

    // Se llama cada vez que alguien hace startService (abrir la app, activar música, cambiar canción)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val elegida = ControlMusica.cancionElegida(this)
        val actual = reproductor
        when {
            actual == null || elegida != fuenteActual -> preparar(elegida)
            preparado && !actual.isPlaying -> empezar()
        }
        // Si Android cierra el servicio por falta de memoria, no hace falta que lo reinicie
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        // Se recuerda por dónde iba para seguir desde ahí al volver a la app
        reproductor?.let { if (preparado) { posicionGuardada = it.currentPosition; fuenteGuardada = fuenteActual } }
        liberar()
        soltarFoco()
        super.onDestroy()
    }

    private fun preparar(uri: Uri?) {
        liberar()
        fuenteActual = uri
        val nuevo = MediaPlayer()
        nuevo.setAudioAttributes(ATRIBUTOS)
        nuevo.isLooping = true
        try {
            if (uri == null) {
                resources.openRawResourceFd(R.raw.musica_oficial).use { archivo ->
                    nuevo.setDataSource(archivo.fileDescriptor, archivo.startOffset, archivo.length)
                }
            } else {
                nuevo.setDataSource(this, uri)
            }
        } catch (e: Exception) {
            // La canción elegida ya no existe o no se puede leer: se vuelve a la oficial
            Log.e(TAG, "No se puede abrir la canción", e)
            nuevo.release()
            if (uri != null) {
                ControlMusica.guardarCancion(this, null)
                preparar(null)
            }
            return
        }
        nuevo.setOnPreparedListener {
            preparado = true
            if (fuenteActual == fuenteGuardada) it.seekTo(posicionGuardada)
            empezar()
        }
        nuevo.setOnErrorListener { _, _, _ -> liberar(); true }
        reproductor = nuevo
        nuevo.prepareAsync()
    }

    // Solo suena si el móvil nos da el foco de audio (por ejemplo, no durante una llamada)
    private fun empezar() {
        if (pedirFoco()) reproductor?.start()
    }

    private fun pedirFoco(): Boolean {
        val resultado = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val peticion = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(ATRIBUTOS)
                .setOnAudioFocusChangeListener(this)
                .build()
            peticionFoco = peticion
            audioManager.requestAudioFocus(peticion)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        return resultado == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun soltarFoco() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            peticionFoco?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
        }
    }

    /** Android nos avisa aquí cuando otra app (o una llamada) quiere el audio. */
    override fun onAudioFocusChange(cambio: Int) {
        val actual = reproductor ?: return
        when (cambio) {
            // Otra app pone su música: paramos. Volverá cuando el jugador vuelva a abrir el juego
            AudioManager.AUDIOFOCUS_LOSS -> {
                pausadaPorFoco = false
                if (actual.isPlaying) actual.pause()
                soltarFoco()
            }
            // Llamada o alarma: pausamos y volveremos solos al acabar
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (actual.isPlaying) {
                    actual.pause()
                    pausadaPorFoco = true
                }
            }
            // Un aviso corto (p. ej. una notificación): bajamos el volumen un momento
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> actual.setVolume(VOLUMEN_BAJO, VOLUMEN_BAJO)
            // Nos devuelven el audio
            AudioManager.AUDIOFOCUS_GAIN -> {
                actual.setVolume(1f, 1f)
                if (pausadaPorFoco) {
                    pausadaPorFoco = false
                    actual.start()
                }
            }
        }
    }

    private fun liberar() {
        reproductor?.release()
        reproductor = null
        preparado = false
    }

    companion object {
        private const val TAG = "MusicaService"
        private const val VOLUMEN_BAJO = 0.2f
        private val ATRIBUTOS: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        // Por dónde iba la canción al mandar la app al fondo
        private var posicionGuardada = 0
        private var fuenteGuardada: Uri? = null
    }
}