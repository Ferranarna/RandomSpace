package com.bugbusters.randomspace.ui.game

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bugbusters.randomspace.R
import com.bugbusters.randomspace.game.Combinacion
import com.bugbusters.randomspace.model.Simbolo

//Imagen del rodillo para cada símbolo
@DrawableRes
fun Simbolo.imagen(): Int = when (this) {
    Simbolo.HAMBURGUESA -> R.drawable.rodillo_hamburguesa
    Simbolo.NEPTUNO -> R.drawable.rodillo_neptuno
    Simbolo.TIERRA -> R.drawable.rodillo_tierra
    Simbolo.SATURNO -> R.drawable.rodillo_saturno
    Simbolo.ESTRELLA -> R.drawable.rodillo_estrella
    Simbolo.OVNI -> R.drawable.rodillo_ovni
    Simbolo.SOL -> R.drawable.rodillo_sol
}

// Frase que se muestra para cada combinación (está en strings.xml para poder traducirla)
@StringRes
fun Combinacion.texto(): Int = when (this) {
    Combinacion.PREMIO_GORDO -> R.string.combo_premio_gordo
    Combinacion.ALINEACION -> R.string.combo_alineacion
    Combinacion.TRIO_PLANETARIO -> R.string.combo_trio_planetario
    Combinacion.APERITIVO -> R.string.combo_aperitivo
    Combinacion.LLUVIA_DE_ESTRELLAS -> R.string.combo_lluvia_estrellas
    Combinacion.DOBLE -> R.string.combo_doble
    Combinacion.INVASION_ALIENIGENA -> R.string.combo_invasion
    Combinacion.NOS_QUEMAMOS -> R.string.combo_quemamos
    Combinacion.NOS_PERSIGUEN -> R.string.combo_persiguen
    Combinacion.QUE_CALOR -> R.string.combo_calor
    Combinacion.NOS_ATACAN -> R.string.combo_atacan
    Combinacion.SIGUE_EXPLORANDO -> R.string.combo_sigue
}