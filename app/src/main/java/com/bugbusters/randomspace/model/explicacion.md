# model/

**Modelos de datos**: las cosas que existen en el juego.

## Qué va aquí
Clases simples que solo guardan información, por ejemplo el jugador, una partida o una puntuación (fecha y valor).

## Tipos de archivo
`data class` de Kotlin (`*.kt`).

## Qué NO va aquí
Lógica, acceso a base de datos ni código de pantalla.

## Regla
Es la capa más baja: la usan todas las demás, pero **ella no depende de ninguna**.
