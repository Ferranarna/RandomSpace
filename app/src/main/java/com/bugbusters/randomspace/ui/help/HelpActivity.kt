package com.bugbusters.randomspace.ui.help

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.ui.menu.MenuDesplegable

/**
 * Pantalla de ayuda "CÓMO JUGAR".
 * El botón VOLVER cierra la pantalla y vuelve a la anterior (la de inicio).
 */
class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        // Textos con colores (HTML) e iconos de moneda ([M]), definidos en strings.xml
        val textos = mapOf(
            R.id.tvAyudaMonedas to R.string.help_monedas_texto,
            R.id.tvAyudaBote to R.string.help_bote_texto,
            R.id.tvAyudaPremio to R.string.help_premio_texto,
            R.id.tvAyudaFin to R.string.help_fin_texto
        )
        for ((vista, texto) in textos) {
            val textView = findViewById<TextView>(vista)
            textView.text = conMonedas(getString(texto))
        }
    }

    /**
     * Convierte un texto HTML en texto con colores y sustituye cada "[M]" por el icono de moneda.
     *
     * @param html texto de strings.xml (con <font color> y marcas [M])
     */
    private fun conMonedas(html: String): CharSequence {
        val marca = "[M]"
        val texto = SpannableStringBuilder(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY))
        val lado = (24 * resources.displayMetrics.density).toInt()
        var pos = texto.indexOf(marca)
        while (pos >= 0) {
            val icono = ContextCompat.getDrawable(this, R.drawable.ic_moneda)!!.mutate()
            icono.setBounds(0, 0, lado, lado)
            texto.setSpan(
                MonedaSpan(icono, (4 * resources.displayMetrics.density).toInt()),
                pos, pos + marca.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            pos = texto.indexOf(marca, pos + marca.length)
        }
        return texto
    }

    /** Icono dentro de un texto, desplazado [desplazamientoPx] píxeles hacia abajo desde la línea base. */
    private class MonedaSpan(icono: android.graphics.drawable.Drawable, private val desplazamientoPx: Int) :
        ImageSpan(icono, ALIGN_BASELINE) {

        override fun draw(
            canvas: android.graphics.Canvas, text: CharSequence?, start: Int, end: Int,
            x: Float, top: Int, y: Int, bottom: Int, paint: android.graphics.Paint
        ) {
            canvas.save()
            canvas.translate(0f, desplazamientoPx.toFloat())
            super.draw(canvas, text, start, end, x, top, y, bottom, paint)
            canvas.restore()
        }
    }
}