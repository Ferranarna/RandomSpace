package com.bugbusters.randomspace.ui.history

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.RandomSpaceApp
import com.bugbusters.randomspace.ui.game.GameActivity
import com.bugbusters.randomspace.ui.menu.MenuDesplegable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * Pantalla de historial: lista de partidas con su fecha y lo que se ganó o perdió.
 */
class HistoryActivity : AppCompatActivity() {

    private val repositorio by lazy { (application as RandomSpaceApp).repositorio }

    // Consultas a la BD en marcha: se cancelan en onDestroy para no tocar una pantalla ya cerrada
    private val suscripciones = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pantalla completa, con margen para que las barras del sistema no tapen nada
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón de menú de la barra superior
        findViewById<View>(R.id.btnMenu).setOnClickListener { v -> MenuDesplegable.mostrar(this, v) }

        // Botón VOLVER: cierra la pantalla y vuelve al inicio
        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }

        // Partidas terminadas del jugador; lo ganado o perdido es saldo final - saldo inicial
        suscripciones.add(
            repositorio.obtenerJugador()
                .flatMapSingle { jugador -> repositorio.obtenerHistorial(jugador.id) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { partidas ->
                        mostrarPartidas(partidas.map {
                            FilaHistorial(
                                GameActivity.formatearFecha(it.fecha),
                                GameActivity.formatearConSigno(it.saldoFinal - it.saldoInicial)
                            )
                        })
                    },
                    { error -> Log.e(TAG, "No se pudo cargar el historial", error) }
                )
        )

        // Título
        findViewById<TextView>(R.id.tvTitulo).setText(R.string.historial_titulo)
    }

    override fun onDestroy() {
        suscripciones.clear()
        super.onDestroy()
    }

    /**
     * Rellena la lista con las partidas recibidas, de la más reciente a la más antigua.
     *
     * @param filas partidas ya formateadas
     */
    private fun mostrarPartidas(filas: List<FilaHistorial>) {
        val lista = findViewById<RecyclerView>(R.id.listaPartidas)
        lista.layoutManager = LinearLayoutManager(this)
        lista.adapter = HistoryAdapter(filas)
    }

    companion object {
        private const val TAG = "HistoryActivity"
    }
}
