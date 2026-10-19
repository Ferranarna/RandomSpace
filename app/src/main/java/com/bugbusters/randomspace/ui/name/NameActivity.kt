package com.bugbusters.randomspace.ui.name

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.ui.welcome.MainActivity

/**
 * Primera pantalla: el jugador escribe su nombre y pasa a la pantalla de inicio.
 */
class NameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pantalla completa, con margen para que las barras del sistema no tapen nada
        enableEdgeToEdge()
        setContentView(R.layout.activity_name)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // TODO: si el jugador ya existe en la base de datos, saltar esta pantalla e ir directo al inicio

        // Botón ¡EMPEZAR!: comprueba que hay nombre y abre la pantalla de inicio
        findViewById<android.view.View>(R.id.btnEmpezar).setOnClickListener {
            val nombre = findViewById<EditText>(R.id.etNombre).text.toString().trim()
            if (nombre.isEmpty()) {
                Toast.makeText(this, R.string.nombre_vacio, Toast.LENGTH_SHORT).show()
            } else {
                // TODO: guardar el jugador en la base de datos (equipo BD)
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("nombre", nombre)
                startActivity(intent)
                finish() // así, al pulsar atrás en el inicio no se vuelve a pedir el nombre
            }
        }
    }
}