package com.bugbusters.randomspace.ui.result

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.ui.game.GameActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.TimeZone

/**
 * Guarda cada victoria como un evento en el calendario del dispositivo, en el día y la hora en que se gana.
 * Usa el Content Provider del calendario (CalendarContract) y trabaja fuera del hilo principal.
 * Se crea al construir la pantalla, porque el selector de permisos hay que registrarlo antes de que arranque.
 */
class CalendarioVictoria(private val activity: AppCompatActivity) {

    // No hay ningún calendario donde se pueda escribir (por ejemplo, en un móvil sin cuenta)
    private class SinCalendario : Exception()

    // Victoria que espera a que el jugador conceda el permiso
    private var pendiente: DatosVictoria? = null

    private val pedirPermisos = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val datos = pendiente
        pendiente = null
        if (datos != null) {
            if (resultado.values.all { it }) insertar(datos) else avisar(R.string.calendario_sin_permiso)
        }
    }

    /** Guarda la victoria en el calendario. Si falta el permiso, lo pide primero. */
    fun guardar(datos: DatosVictoria) {
        if (tienePermisos()) {
            insertar(datos)
        } else {
            pendiente = datos
            pedirPermisos.launch(PERMISOS)
        }
    }

    private fun tienePermisos() = PERMISOS.all {
        ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
    }

    // Busca un calendario y crea el evento, _Todo en un hilo de segundo plano
    private fun insertar(datos: DatosVictoria) {
        Completable.fromAction {
            val calendarioId = buscarCalendario() ?: throw SinCalendario()
            val valores = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarioId)
                put(CalendarContract.Events.TITLE, activity.getString(R.string.calendario_titulo))
                put(
                    CalendarContract.Events.DESCRIPTION,
                    activity.getString(
                        R.string.calendario_descripcion,
                        GameActivity.formatear(datos.premio),
                        GameActivity.formatear(datos.saldoFinal)
                    )
                )
                put(CalendarContract.Events.DTSTART, datos.fecha)
                put(CalendarContract.Events.DTEND, datos.fecha + DURACION_EVENTO)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            activity.contentResolver.insert(CalendarContract.Events.CONTENT_URI, valores)
                ?: error("El calendario no ha guardado el evento")
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { avisar(R.string.calendario_guardado) },
                { error ->
                    Log.e(TAG, "No se pudo guardar en el calendario", error)
                    avisar(if (error is SinCalendario) R.string.calendario_sin_calendario else R.string.calendario_error)
                }
            )
    }

    // Primer calendario visible en el que se puede escribir, o null si no hay ninguno
    private fun buscarCalendario(): Long? {
        val columnas = arrayOf(CalendarContract.Calendars._ID)
        val filtro = "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ${CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR}" +
                " AND ${CalendarContract.Calendars.VISIBLE} = 1"
        activity.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI, columnas, filtro, null, "${CalendarContract.Calendars._ID} ASC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) return cursor.getLong(0)
        }
        return null
    }

    private fun avisar(@StringRes mensaje: Int) {
        if (!activity.isDestroyed) Toast.makeText(activity, mensaje, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        const val TAG = "CalendarioVictoria"
        const val DURACION_EVENTO = 0L
        val PERMISOS = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
    }
}