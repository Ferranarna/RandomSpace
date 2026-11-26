package com.bugbusters.randomspace.ui.menu

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.bugbusters.randomspace.R
import android.content.Intent
import com.bugbusters.randomspace.ui.help.HelpActivity
import com.bugbusters.randomspace.ui.prizes.PrizesActivity
import com.bugbusters.randomspace.ui.history.HistoryActivity
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bugbusters.randomspace.audio.ControlMusica

/**
 * Menú desplegable del botón de la hamburguesa, compartido por todas las pantallas.
 */
object MenuDesplegable {

    /**
     * Muestra el menú (layout popup_menu.xml) bajo [anchor], alineado a su borde derecho.
     * Se cierra al elegir una opción o al tocar fuera.
     *
     * @param activity pantalla desde la que se abre
     * @param anchor vista bajo la que aparece (el botón de la hamburguesa)
     */

    /**
     * Abre el selector de archivos del sistema para elegir un audio.
     * No pide permisos: el jugador elige el archivo y Android nos deja leer solo ese.
     */
    private fun elegirCancion(activity: Activity) {
        val registro = (activity as ComponentActivity).activityResultRegistry
        lateinit var lanzador: ActivityResultLauncher<Array<String>>
        lanzador = registro.register("elegir_cancion", ActivityResultContracts.OpenDocument()) { uri ->
            lanzador.unregister()   // solo se usa una vez
            if (uri == null) return@register   // el jugador ha cancelado
            try {
                // Para poder seguir leyéndola cuando se cierre y se vuelva a abrir la app
                activity.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                // Algunos selectores no lo permiten: la canción sonará hasta que se cierre la app
            }
            ControlMusica.elegirCancion(activity, uri)
            Toast.makeText(activity, R.string.musica_cancion_elegida, Toast.LENGTH_SHORT).show()
        }
        lanzador.launch(arrayOf("audio/*"))
    }

    fun mostrar(activity: Activity, anchor: View) {
        val densidad = activity.resources.displayMetrics.density
        val ancho = (220 * densidad).toInt()

        val contenido = LayoutInflater.from(activity).inflate(R.layout.popup_menu, null)
        val popup = PopupWindow(contenido, ancho, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        // Fondo transparente: necesario para que se cierre al tocar fuera en todas las versiones
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Pantalla Ayuda
        contenido.findViewById<View>(R.id.opcionAyuda).setOnClickListener {
            popup.dismiss()
            abrir(activity, HelpActivity::class.java)
        }

        // Pantalla Premios
        contenido.findViewById<View>(R.id.opcionPremios).setOnClickListener {
            popup.dismiss()
            abrir(activity, PrizesActivity::class.java)
        }

        // Pantalla Historial
        contenido.findViewById<View>(R.id.opcionHistorial).setOnClickListener {
            popup.dismiss()
            abrir(activity, HistoryActivity::class.java)
        }

        // Música: el texto dice si está activada; al pulsar se activa o desactiva
        val opcionMusica = contenido.findViewById<TextView>(R.id.opcionMusica)
        opcionMusica.setText(if (ControlMusica.estaActivada(activity)) R.string.menu_musica_on else R.string.menu_musica_off)
        opcionMusica.setOnClickListener {
            popup.dismiss()
            ControlMusica.alternar(activity)
        }

        // Elegir una canción del móvil
        contenido.findViewById<View>(R.id.opcionCancion).setOnClickListener {
            popup.dismiss()
            elegirCancion(activity)
        }

        // Volver a la melodía oficial
        contenido.findViewById<View>(R.id.opcionOficial).setOnClickListener {
            popup.dismiss()
            ControlMusica.elegirCancion(activity, null)
        }

        // Idioma: el del dispositivo, español o inglés
        contenido.findViewById<View>(R.id.opcionIdioma).setOnClickListener {
            popup.dismiss()
            Idioma.elegir(activity)
        }

        // Salir de la app: cierra todas las pantallas abiertas
        contenido.findViewById<View>(R.id.opcionSalir).setOnClickListener {
            popup.dismiss()
            activity.finishAffinity()
        }

        // Desplazamiento horizontal negativo: el borde derecho del menú coincide con el del botón
        popup.showAsDropDown(anchor, anchor.width - ancho, (8 * densidad).toInt())
    }

    /**
     * Abre una pantalla del menú desde [activity].
     * - Si ya estamos en ella, no hace nada (no se abre otra vez encima).
     * - Si [activity] es también una pantalla del menú, se cierra: así VOLVER no pasa
     *   de una pantalla del menú a otra, sino a lo que había antes de entrar al menú.
     */
    private fun abrir(activity: Activity, destino: Class<out Activity>) {
        if (activity.javaClass == destino) return
        activity.startActivity(Intent(activity, destino))
        if (activity is HelpActivity || activity is PrizesActivity || activity is HistoryActivity) {
            activity.finish()
        }
    }

}