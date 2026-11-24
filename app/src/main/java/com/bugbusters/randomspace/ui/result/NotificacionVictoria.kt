package com.bugbusters.randomspace.ui.result

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.ui.game.GameActivity

/**
 * Notificación que avisa de una victoria. Al pulsarla abre la pantalla de victoria con sus datos.
 * El permiso (Android 13+) se pide al entrar a jugar, en GameActivity: si se pidiera aquí,
 * chocaría con el del calendario, que se pide en el mismo momento.
 */
class NotificacionVictoria(private val activity: AppCompatActivity) {

    @SuppressLint("MissingPermission")  // se comprueba en puedeNotificar
    fun mostrar(datos: DatosVictoria) {
        if (!puedeNotificar(activity)) return
        crearCanal()

        val texto = activity.getString(
            R.string.notificacion_texto,
            GameActivity.formatear(datos.premio),
            GameActivity.formatear(datos.saldoFinal),
            GameActivity.formatearDuracion(datos.duracion)
        )

        val alPulsar = PendingIntent.getActivity(
            activity,
            datos.partidaId.toInt(),     // uno distinto por victoria, para que no se pisen
            intentDatos(datos),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(activity, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setLargeIcon(BitmapFactory.decodeResource(activity.resources, R.drawable.ic_launcher))
            .setContentTitle(activity.getString(R.string.notificacion_titulo))
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(alPulsar)
            .setAutoCancel(true)         // desaparece al pulsarla
            // Antes de Android 8 no hay canales: la ventana emergente se pide así (prioridad alta y con sonido)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()

        NotificationManagerCompat.from(activity).notify(datos.partidaId.toInt(), notificacion)
    }

    // La pantalla de victoria con los mismos datos que le manda GameActivity
    private fun intentDatos(datos: DatosVictoria) = Intent(activity, VictoryActivity::class.java)
        .putExtra(GameActivity.EXTRA_PARTIDA_ID, datos.partidaId)
        .putExtra(GameActivity.EXTRA_BOTE_BASE, datos.boteBase)
        .putExtra(GameActivity.EXTRA_MULTIPLICADOR, datos.multiplicador)
        .putExtra(GameActivity.EXTRA_PREMIO, datos.premio)
        .putExtra(GameActivity.EXTRA_SALDO, datos.saldoFinal)
        .putExtra(GameActivity.EXTRA_SALDO_ANTES, datos.saldoFinal - datos.premio)
        .putExtra(EXTRA_DURACION, datos.duracion)
        .putExtra(EXTRA_DESDE_NOTIFICACION, true)

    // Desde Android 8 cada notificación tiene que ir en un canal (el usuario lo ve en Ajustes)
    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL, activity.getString(R.string.notificacion_canal), NotificationManager.IMPORTANCE_HIGH
            )
            activity.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    companion object {
        private const val CANAL = "victorias_emergentes"

        /** Extra para que la pantalla de victoria sepa que se abre desde la notificación. */
        const val EXTRA_DESDE_NOTIFICACION = "desde_notificacion"
        const val EXTRA_DURACION = "duracion"

        /** Antes de Android 13 no hace falta permiso. */
        fun puedeNotificar(context: Context) =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
    }
}