# data/local/

Acceso a **SQLite** en el dispositivo.

## Qué va aquí
- Clase que crea y actualiza la base de datos (`SQLiteOpenHelper` o equivalente).
- Definición de tablas y columnas (nombres como constantes).
- Clases de acceso (DAO) con las consultas de inserción y lectura.

## Tipos de archivo
`*DbHelper.kt`, `*Contract.kt`, `*Dao.kt`.

## Qué NO va aquí
Pantallas ni lógica de juego. Esta carpeta no decide hilos ni RxJava: solo ejecuta las operaciones.
