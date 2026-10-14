# ui/

Capa de **interfaz de usuario**: todo lo que el jugador ve y toca.

## Qué va aquí
Un subpaquete por pantalla (`welcome/`, `game/`…). Cada pantalla contiene sus clases de interfaz.

## Tipos de archivo
- `Activity` y `Fragment` (`*Activity.kt`, `*Fragment.kt`).
- Adaptadores de listas (`*Adapter.kt`).
- Vistas personalizadas (`*View.kt`).
- `ViewModel`, si el equipo decide usarlos.

## Qué NO va aquí
- Reglas del juego (van en `game/`).
- Consultas SQL (van en `data/`).

## Regla
La UI **muestra y recoge eventos**, y delega todo lo demás. Puede conocer `game/`, `data/repository/` y `model/`, pero ninguna de esas capas puede importar `ui/`.
