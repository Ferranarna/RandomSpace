package com.bugbusters.randomspace.ui.menu

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.bugbusters.randomspace.R

/**
 * Cambio manual del idioma de la app: idioma del dispositivo, español o inglés.
 * Usa la API de AndroidX (AppCompatDelegate): cambia solo el idioma de esta app, recrea las pantallas
 * abiertas para que se vea al momento y lo recuerda al volver a abrirla.
 */
object Idioma {

    // Mismo orden que las opciones del diálogo. "" = sin idioma propio: se sigue el del dispositivo
    private val etiquetas = arrayOf("", "es", "en")

    fun elegir(activity: Activity) {
        val opciones = arrayOf(
            activity.getString(R.string.idioma_sistema),
            activity.getString(R.string.idioma_es),
            activity.getString(R.string.idioma_en)
        )
        val actual = etiquetas.indexOf(idiomaElegido()).coerceAtLeast(0)

        AlertDialog.Builder(activity)
            .setTitle(R.string.idioma_titulo)
            .setSingleChoiceItems(opciones, actual) { dialogo, elegida ->
                dialogo.dismiss()
                if (elegida != actual) {
                    // Lista vacía ("") = volver al idioma del dispositivo
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(etiquetas[elegida]))
                }
            }
            .show()
    }

    // Idioma elegido en la app ("es", "en") o "" si sigue al del dispositivo
    private fun idiomaElegido(): String =
        AppCompatDelegate.getApplicationLocales()[0]?.language ?: ""
}