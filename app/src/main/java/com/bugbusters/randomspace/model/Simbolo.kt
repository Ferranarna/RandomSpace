package com.bugbusters.randomspace.model

enum class Simbolo {
    HAMBURGUESA, LUNA, TIERRA, JUPITER, ESTRELLA, OVNI, SOL;

    val esPlaneta: Boolean
        get() = this == LUNA || this == TIERRA || this == JUPITER
}