package com.bugbusters.randomspace.ui.welcome

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.RandomSpaceApp
import com.bugbusters.randomspace.data.repository.DatosInicio
import com.bugbusters.randomspace.ui.menu.MenuDesplegable
import android.content.Intent
import com.bugbusters.randomspace.ui.game.GameActivity
import com.bugbusters.randomspace.ui.history.HistoryActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * Pantalla de inicio de Random Space.
 *
 * Muestra el bote, el usuario con sus monedas y sus 5 mejores partidas,
 * y da acceso a las pantallas de juego e historial y al menú de opciones.
 */
class MainActivity : AppCompatActivity() {

    private val repositorio by lazy { (application as RandomSpaceApp).repositorio }

    // Consultas a la BD en marcha: se cancelan en onDestroy para no tocar una pantalla ya cerrada
    private val suscripciones = CompositeDisposable()

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

        // Mientras llegan los datos de la BD: el nombre que manda NameActivity y el resto vacío
        mostrarDatos(
            usuario = intent.getStringExtra("nombre") ?: "",
            monedas = "",
            bote = "",
            fechas = emptyList(),
            puntos = emptyList()
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

    // En onResume y no en onCreate: al volver de una partida el saldo, el bote y el top han cambiado
    override fun onResume() {
        super.onResume()
        suscripciones.add(
            repositorio.obtenerDatosInicio(MEJORES_PARTIDAS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { datos -> mostrarDatos(datos) },
                    { error -> Log.e(TAG, "No se pudieron cargar los datos de inicio", error) }
                )
        )
    }

    override fun onDestroy() {
        suscripciones.clear()
        super.onDestroy()
    }

    // Convierte los datos de la BD en los textos de la pantalla
    private fun mostrarDatos(datos: DatosInicio) {
        mostrarDatos(
            usuario = datos.jugador.nombre,
            monedas = GameActivity.formatear(datos.jugador.saldo),
            bote = GameActivity.formatear(datos.bote.cantidad),
            fechas = datos.mejoresPartidas.map { GameActivity.formatearFecha(it.fecha) },
            puntos = datos.mejoresPartidas.map { GameActivity.formatear(it.resultado) }
        )
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

    companion object {
        private const val TAG = "MainActivity"
        private const val MEJORES_PARTIDAS = 5
    }
}
