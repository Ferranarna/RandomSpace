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