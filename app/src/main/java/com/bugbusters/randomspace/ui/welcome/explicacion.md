# ui/welcome/

Pantalla de **bienvenida** (pantalla inicial de la app).

## Qué va aquí
- La Activity o Fragment de bienvenida.
- La lista o adaptador de las **mejores partidas** (fecha y puntuación), que se muestran en esta pantalla.
- Los botones de navegación hacia la partida.

## Tipos de archivo
`*Activity.kt`, `*Fragment.kt`, `*Adapter.kt`, `*ViewModel.kt`.

## Nota
Esta pantalla pide los datos al repositorio (`data/repository/`) y no accede directamente a SQLite. Su layout vive en `res/layout/`.
