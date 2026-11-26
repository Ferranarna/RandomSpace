package com.bugbusters.randomspace.ui.help

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.ui.menu.MenuDesplegable

/**
 * Pantalla de ayuda: una página HTML en un WebView con el mismo estilo que la app.
 * El HTML está en res/raw (inglés) y res/raw-es (español): Android elige el del idioma de la app.
 */
class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Se lee con los recursos de ESTA pantalla, que tienen el idioma elegido en la app.
        // Y antes de crear el WebView: en algunas versiones, crear el primer WebView restablece el idioma del sistema
        val html = resources.openRawResource(R.raw.ayuda).bufferedReader().use { it.readText() }

        // Pantalla completa, con margen para que las barras del sistema no tapen nada
        enableEdgeToEdge()
        setContentView(R.layout.activity_help)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón de menú de la barra superior
        findViewById<View>(R.id.btnMenu).setOnClickListener { v -> MenuDesplegable.mostrar(this, v) }

        // Botón VOLVER: cierra esta pantalla
        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }

        // Título
        findViewById<TextView>(R.id.tvTitulo).setText(R.string.help_titulo)

        val web = findViewById<WebView>(R.id.webAyuda)
        // Transparente: mientras carga se ve el fondo de la pantalla, sin destello blanco
        web.setBackgroundColor(Color.TRANSPARENT)
        // La página no usa JavaScript: desactivado (es lo predeterminado, se deja explícito por seguridad)
        web.settings.javaScriptEnabled = false
        // Base "file:///android_res/": la página usa las fuentes, imágenes y estilos de res/ sin copiarlos
        web.loadDataWithBaseURL("file:///android_res/", html, "text/html", "utf-8", null)
    }
}