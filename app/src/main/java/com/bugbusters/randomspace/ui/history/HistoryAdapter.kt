package com.bugbusters.randomspace.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bugbusters.randomspace.R

/**
 * Una fila del historial.
 */
data class FilaHistorial(val fecha: String, val resultado: String, val victoria: Boolean)

/**
 * Pinta la lista de partidas del historial. Las ganancias salen en verde y las pérdidas en rojo.
 */
class HistoryAdapter(private val filas: List<FilaHistorial>) :
    RecyclerView.Adapter<HistoryAdapter.FilaViewHolder>() {

    /** Guarda las vistas de una fila para no buscarlas cada vez que se reutiliza. */
    class FilaViewHolder(vista: android.view.View) : RecyclerView.ViewHolder(vista) {
        val tvFecha: TextView = vista.findViewById(R.id.tvFecha)
        val tvResultado: TextView = vista.findViewById(R.id.tvResultado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilaViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_partida, parent, false)
        return FilaViewHolder(vista)
    }

    override fun onBindViewHolder(holder: FilaViewHolder, position: Int) {
        val fila = filas[position]
        holder.tvFecha.text = fila.fecha
        holder.tvResultado.text = fila.resultado
        // Verde si la partida fue una victoria, rojo si fue una derrota
        val color = if (fila.victoria) R.color.green else R.color.red
        holder.tvResultado.setTextColor(ContextCompat.getColor(holder.itemView.context, color))
    }

    override fun getItemCount(): Int = filas.size
}