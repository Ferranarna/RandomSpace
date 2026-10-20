package com.bugbusters.randomspace.ui.history

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.ui.menu.MenuDesplegable

/**
 * Pantalla de historial: lista de partidas con su fecha y lo que se ganó o perdió.
 */
class HistoryActivity : AppCompatActivity() {

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

        // TODO: DATOS INVENTADOS A SUSTITUIR (es para ver como queda, es temporal)
        mostrarPartidas(
            listOf(
                FilaHistorial("20/10/2026", "+200"),
                FilaHistorial("19/10/2026", "-150"),
                FilaHistorial("19/10/2026", "+10"),
                FilaHistorial("18/10/2026", "-50"),
                FilaHistorial("18/10/2026", "+500"),
                FilaHistorial("17/10/2026", "-100"),
                FilaHistorial("17/10/2026", "+30"),
                FilaHistorial("16/10/2026", "-300"),
                FilaHistorial("16/10/2026", "+1.500"),
                FilaHistorial("15/10/2026", "-600")
            )
        )

        // Título
        findViewById<TextView>(R.id.tvTitulo).setText(R.string.historial_titulo)
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
}