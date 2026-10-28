package com.bugbusters.randomspace.model

enum class Simbolo {
    HAMBURGUESA, NEPTUNO, TIERRA, SATURNO, ESTRELLA, OVNI, SOL;

    val esPlaneta: Boolean
        get() = this == NEPTUNO || this == TIERRA || this == SATURNO
}