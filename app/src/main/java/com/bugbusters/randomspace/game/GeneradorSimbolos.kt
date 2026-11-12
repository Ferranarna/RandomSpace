package com.bugbusters.randomspace.game

import com.bugbusters.randomspace.model.Simbolo
import kotlin.random.Random

fun interface GeneradorSimbolos {
    fun siguiente(): Simbolo
}

class GeneradorSimbolosAleatorio(
    private val random: Random = Random.Default
) : GeneradorSimbolos {
    override fun siguiente(): Simbolo = Simbolo.entries.random(random)
}