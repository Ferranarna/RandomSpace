# ui/game/

Pantalla de la **partida**: interfaz gráfica y animaciones.

## Qué va aquí
- La Activity o Fragment de la partida.
- La vista de dibujo con **Canvas** (`*View.kt`) y el código de animación.
- La lógica de pintar: dibujar, animar, reaccionar al toque.

## Tipos de archivo
`*Activity.kt`, `*Fragment.kt`, `*View.kt`, `*ViewModel.kt`.

## Qué NO va aquí
Decidir si el jugador gana o pierde, ni generar números aleatorios: eso es `game/`. Esta capa le pregunta el resultado a `game/` y lo dibuja.

## Nota
Dibujar en `onDraw` debe ser ligero: no crear objetos nuevos en cada fotograma (criterio de optimización de memoria/CPU de la rúbrica).
