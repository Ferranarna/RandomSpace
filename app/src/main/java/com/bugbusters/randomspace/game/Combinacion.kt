package com.bugbusters.randomspace.game

enum class Combinacion(val valorBase: Int) {
    PREMIO_GORDO(0), // no usa valor base: se paga con el bote
    ALINEACION(500),
    TRIO_PLANETARIO(250),
    APERITIVO(200),
    LLUVIA_DE_ESTRELLAS(50),
    DOBLE(10),
    INVASION_ALIENIGENA(-200),
    NOS_QUEMAMOS(-200),
    NOS_PERSIGUEN(-100),
    QUE_CALOR(-100),
    NOS_ATACAN(-50),
    SIGUE_EXPLORANDO(0)
}