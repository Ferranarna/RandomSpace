package com.bugbusters.randomspace

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.PopupWindow
import android.view.ViewGroup
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

/**
 * Pantalla de inicio de Random Space.
 *
 * Muestra el bote, el usuario con sus monedas y sus 3 mejores partidas,
 * y da acceso a las pantallas de juego e historial y al menú de opciones.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dibuja la app a pantalla completa (bajo la barra de estado y la de navegación)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Añade un margen interior igual al tamaño de las barras del sistema (para evitar que se tapen botones)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Usa nuestra Toolbar como barra de acción y oculta el título por defecto
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // TODO: DATOS INVENTADOS A SUSTITUIR (es para ver como queda, es temporal)
        mostrarDatos(
            usuario = "CYBER_GAMBLER", monedas = "1.000", bote = "10.000",
            fechas = listOf("16/10/2026", "15/10/2026", "14/10/2026"),
            puntos = listOf("3.500", "2.800", "2.100")
        )

        // TODO: Botón ¡JUGAR!: abrirá la pantalla de juego (temporal)
        findViewById<View>(R.id.btnJugar).setOnClickListener {
            Toast.makeText(this, "Jugar", Toast.LENGTH_SHORT).show()
        }

        // TODO: Botón HISTORIAL: abrirá el registro de partidas jugadas (temporal)
        findViewById<View>(R.id.btnHistorial).setOnClickListener {
            Toast.makeText(this, "Historial", Toast.LENGTH_SHORT).show()
        }

        // Botón de menú (hamburguesa de la derecha): despliega el menú de opciones
        findViewById<View>(R.id.btnMenu).setOnClickListener { v -> mostrarMenu(v) }
    }

    /**
     * Muestra el menú desplegable (layout popup_menu.xml) bajo el botón de menú,
     * alineado a su borde derecho. Se cierra al elegir una opción o al tocar fuera.
     *
     * @param anchor vista bajo la que aparece el menú (el botón de la hamburguesa)
     */
    private fun mostrarMenu(anchor: View) {
        val densidad = resources.displayMetrics.density
        val ancho = (220 * densidad).toInt()

        val contenido = layoutInflater.inflate(R.layout.popup_menu, null)
        val popup = PopupWindow(contenido, ancho, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        // Fondo transparente: necesario para que se cierre al tocar fuera en todas las versiones
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // TODO: abrir la pantalla Premios
        contenido.findViewById<View>(R.id.opcionPremios).setOnClickListener {
            popup.dismiss()
        }

        // TODO: abrir la pantalla Ayuda
        contenido.findViewById<View>(R.id.opcionAyuda).setOnClickListener {
            popup.dismiss()
        }

        // Desplazamiento horizontal negativo: el borde derecho del menú coincide con el del botón
        popup.showAsDropDown(anchor, anchor.width - ancho, (8 * densidad).toInt())
    }

    /**
     * Rellena la pantalla con los datos recibidos.
     * Solo cambia textos: el diseño está en activity_main.xml.
     *
     * @param usuario nombre del jugador
     * @param monedas saldo del jugador (ya formateado, p. ej. "1.000")
     * @param bote bote actual (ya formateado)
     * @param fechas fechas de las mejores partidas, de mejor a peor (máx. 3)
     * @param puntos puntuaciones de esas partidas, en el mismo orden que las fechas
     */
    private fun mostrarDatos(
        usuario: String, monedas: String, bote: String,
        fechas: List<String>, puntos: List<String>
    ) {
        findViewById<TextView>(R.id.tvUsuario).text = usuario
        findViewById<TextView>(R.id.tvMonedas).text = monedas
        findViewById<TextView>(R.id.tvBote).text = bote

        // Las 3 filas de "tus mejores partidas": si hay menos de 3 datos, la fila queda vacía
        val tvFechas = listOf(R.id.tvFecha1, R.id.tvFecha2, R.id.tvFecha3)
        val tvPuntos = listOf(R.id.tvPuntos1, R.id.tvPuntos2, R.id.tvPuntos3)
        for (i in 0..2) {
            findViewById<TextView>(tvFechas[i]).text = fechas.getOrElse(i) { "" }
            findViewById<TextView>(tvPuntos[i]).text = puntos.getOrElse(i) { "" }
        }
    }
}