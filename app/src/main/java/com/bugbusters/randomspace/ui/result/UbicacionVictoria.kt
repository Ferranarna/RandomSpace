package com.bugbusters.randomspace.ui.result

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.bugbusters.randomspace.RandomSpaceApp
import java.util.concurrent.TimeUnit
import android.app.Activity
import com.bugbusters.randomspace.data.repository.RandomSpaceRepository

/**
 * Guarda en la BD dónde se ganó la partida: ubicación aproximada (sin GPS) y una sola lectura.
 * El permiso NO se pide aquí: se pide al entrar a jugar (GameActivity) junto al de notificaciones,
 * para que no choque con el del calendario. Sin permiso, la victoria se queda sin ubicación (NULL).
 */
class UbicacionVictoria(private val activity: Activity) {

    /**
     * Se llama desde onCreate, cuando la pantalla ya tiene contexto (en el constructor aún no lo tiene).
     * Solo pasa a [leerYGuardar] el contexto de la aplicación: la lectura puede llegar después de cerrar
     * la pantalla y no debe retener la Activity destruida (fuga de memoria).
     */
    fun guardar(datos: DatosVictoria) {
        if (datos.partidaId < 1) {
            Log.w(TAG, "Victoria sin id de partida: no se guarda la ubicación")
            return
        }
        leerYGuardar(activity.applicationContext, datos.partidaId)
    }

    companion object {
        private const val TAG = "UbicacionVictoria"

        // Una ubicación de hace menos de 2 minutos sigue valiendo para saber dónde se ganó
        private val ANTIGUEDAD_MAXIMA_NANOS = TimeUnit.MINUTES.toNanos(2)

        /** true si el jugador ha concedido la ubicación aproximada. */
        fun tienePermiso(context: Context) =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

        // En el companion: lo que se queda esperando la ubicación solo conoce app y partidaId, nunca la Activity
        @SuppressLint("MissingPermission")  // se comprueba en tienePermiso
        private fun leerYGuardar(app: Context, partidaId: Long) {
            if (!tienePermiso(app)) return
            val locationManager = app.getSystemService(LocationManager::class.java)
            if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
                Log.w(TAG, "Ubicación desactivada")
                return
            }
            val proveedores = proveedoresDisponibles(locationManager)
            if (proveedores.isEmpty()) {
                Log.w(TAG, "Sin proveedor de ubicación disponible")
                return
            }
            val repositorio = (app as RandomSpaceApp).repositorio

            // 1) La más reciente de las que ya conoce el sistema (de cualquier proveedor). Si es reciente, se usa
            val ultima = proveedores
                .mapNotNull { locationManager.getLastKnownLocation(it) }
                .maxByOrNull { it.elapsedRealtimeNanos }
            if (ultima != null && esReciente(ultima)) {
                guardarEnBd(repositorio, partidaId, ultima)
                return
            }

            // 2) Si no, se pide UNA lectura (el sistema la detiene sola). Si no llega, se guarda la última
            //    conocida aunque sea antigua: mejor saber dónde estaba hace un rato que no saber nada
            LocationManagerCompat.getCurrentLocation(
                locationManager, proveedores.first(), null, ContextCompat.getMainExecutor(app)
            ) { ubicacion ->
                val elegida = ubicacion ?: ultima
                if (elegida != null) guardarEnBd(repositorio, partidaId, elegida)
                else Log.w(TAG, "No se ha podido obtener la ubicación")
            }
        }

        // Por orden de preferencia: red (wifi/antenas, sin GPS) y "fused". Desde Android 12, con permiso aproximado
        // también se puede usar el GPS (el sistema devuelve la ubicación aproximada): en el emulador es el único que hay
        private fun proveedoresDisponibles(locationManager: LocationManager): List<String> = buildList {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) add(LocationManager.NETWORK_PROVIDER)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)) add(LocationManager.FUSED_PROVIDER)
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) add(LocationManager.GPS_PROVIDER)
            }
        }

        private fun esReciente(ubicacion: Location) =
            SystemClock.elapsedRealtimeNanos() - ubicacion.elapsedRealtimeNanos < ANTIGUEDAD_MAXIMA_NANOS

        // Hilo de escrituras del repositorio. No se cancela al salir: la ubicación no debe perderse
        private fun guardarEnBd(repositorio: RandomSpaceRepository, partidaId: Long, ubicacion: Location) {
            repositorio.guardarUbicacion(partidaId, ubicacion.latitude, ubicacion.longitude)
                .subscribe({}, { error -> Log.e(TAG, "No se pudo guardar la ubicación", error) })
        }
    }
}