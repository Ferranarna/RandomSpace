# data/repository/

**Repositorio**: intermediario entre la app y el origen de los datos.

## Qué va aquí
- Clases que exponen las operaciones de datos (guardar partida, obtener mejores puntuaciones, actualizar monedas).
- **RxJava**: las llamadas a SQLite se envuelven aquí en `Single`, `Completable` u `Observable`, y se decide en qué hilo se ejecutan.

## Tipos de archivo
`*Repository.kt`.

## Por qué existe
- La UI no toca SQLite directamente: no bloquea el hilo principal ni conoce SQL.
- Si cambia el origen de los datos (en P3 se añadirá un backend), solo se toca esta capa.
