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
            val proveedor = elegirProveedor(locationManager) ?: run {
                Log.w(TAG, "Ubicación desactivada o sin proveedor disponible")
                return
            }
            val repositorio = (app as RandomSpaceApp).repositorio

            // 1) Si el sistema ya tiene una ubicación reciente, se usa: no hay que encender nada
            val reciente = locationManager.getLastKnownLocation(proveedor)
            if (reciente != null && esReciente(reciente)) {
                guardarEnBd(repositorio, partidaId, reciente)
                return
            }

            // 2) Si no, se pide UNA lectura; el sistema la detiene sola (no queda ningún listener gastando batería)
            LocationManagerCompat.getCurrentLocation(
                locationManager, proveedor, null, ContextCompat.getMainExecutor(app)
            ) { ubicacion ->
                if (ubicacion != null) guardarEnBd(repositorio, partidaId, ubicacion)
                else Log.w(TAG, "No se ha podido obtener la ubicación")
            }
        }

        // Red (wifi/antenas): aproximada y sin GPS. Si no existe (p. ej. en el emulador), "fused" (Android 12+)
        private fun elegirProveedor(locationManager: LocationManager): String? {
            if (!LocationManagerCompat.isLocationEnabled(locationManager)) return null
            return when {
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER) -> LocationManager.FUSED_PROVIDER
                else -> null
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