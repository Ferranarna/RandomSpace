# game/

**Lógica del juego de azar**: las reglas y el resultado.

## Qué va aquí
- Generación de números aleatorios.
- Reglas: qué combinación gana, cuánto se gana o se pierde.
- Cálculo de las monedas del jugador tras cada partida.
- Interfaces que permitan ampliar la complejidad (nuevas reglas, nuevos modos) sin modificar lo ya hecho.

## Tipos de archivo
Clases, interfaces y `enum class` de Kotlin (`*.kt`).

## Regla importante
Esta carpeta **no importa nada de `android.*`** (ni `Context`, ni vistas, ni SQLite). Así:
- se puede comprobar con tests unitarios en el ordenador, sin emulador;
- es fácil de ampliar (rúbrica: nivel 6 de "Lógica del juego");
- el algoritmo se puede optimizar sin tocar la interfaz (nivel 8).
