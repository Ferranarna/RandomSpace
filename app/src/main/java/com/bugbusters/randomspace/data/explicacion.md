# data/

Capa de **datos**: guardar y recuperar información.

## Qué va aquí
Dos subcarpetas:
- `local/` guarda y lee en la base de datos del dispositivo (SQLite).
- `repository/` es la puerta de entrada para el resto de la app.

## Regla
El resto de la app (UI y juego) solo habla con `repository/`. Nadie fuera de `data/` sabe cómo ni dónde se guardan los datos.
