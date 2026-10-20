package com.bugbusters.randomspace.ui.welcome

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.ui.menu.MenuDesplegable
import android.content.Intent
import com.bugbusters.randomspace.ui.game.GameActivity
import com.bugbusters.randomspace.ui.history.HistoryActivity

/**
 * Pantalla de inicio de Random Space.
 *
 * Muestra el bote, el usuario con sus monedas y sus 5 mejores partidas,
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
            usuario = intent.getStringExtra("nombre") ?: "CYBER_GAMBLER",
            monedas = "1.200",
            bote = "1.300",
            fechas = listOf("19/10/2026", "18/10/2026", "17/10/2026", "16/10/2026", "15/10/2026"),
            puntos = listOf("5.100", "3.500", "2.800", "2.100", "900")
        )

        // Botón ¡JUGAR!: abre la pantalla de juego
        findViewById<View>(R.id.btnJugar).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        // Botón HISTORIAL: abre el registro de partidas jugadas
        findViewById<View>(R.id.btnHistorial).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Botón de menú (hamburguesa de la derecha): despliega el menú de opciones
        findViewById<View>(R.id.btnMenu).setOnClickListener { v -> MenuDesplegable.mostrar(this, v) }
    }

    /**
     * Rellena la pantalla con los datos recibidos.
     * Solo cambia textos: el diseño está en activity_main.xml.
     */
    private fun mostrarDatos(
        usuario: String, monedas: String, bote: String,
        fechas: List<String>, puntos: List<String>
    ) {
        findViewById<TextView>(R.id.tvUsuario).text = usuario
        findViewById<TextView>(R.id.tvMonedas).text = monedas
        findViewById<TextView>(R.id.tvBote).text = bote

        // Las filas de "tus mejores partidas"
        val tvFechas = listOf(R.id.tvFecha1, R.id.tvFecha2, R.id.tvFecha3, R.id.tvFecha4, R.id.tvFecha5)
        val tvPuntos = listOf(R.id.tvPuntos1, R.id.tvPuntos2, R.id.tvPuntos3, R.id.tvPuntos4, R.id.tvPuntos5)
        for (i in 0..4) {
            findViewById<TextView>(tvFechas[i]).text = fechas.getOrElse(i) { "" }
            val tvPunto = findViewById<TextView>(tvPuntos[i])
            tvPunto.text = puntos.getOrElse(i) { "" }
            // El número y su moneda están en el mismo contenedor: se oculta el contenedor entero
            // (INVISIBLE mantiene el hueco para que las filas no se muevan)
            (tvPunto.parent as View).visibility =
                if (i < puntos.size) View.VISIBLE else View.INVISIBLE
        }
    }
}