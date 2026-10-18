# ui/menu/

**Menú desplegable** del botón de la hamburguesa, compartido por todas las pantallas.

## Qué va aquí
- `MenuDesplegable.kt`: muestra el menú bajo el botón y decide qué hace cada opción (abrir Premios, Ayuda...).

## Cómo añadir una opción
1. Nuevo `TextView` con `style="@style/OpcionMenu"` en `res/layout/popup_menu.xml`, con su id.
2. Su texto en `strings.xml`.
3. Su clic en `MenuDesplegable.mostrar(...)`, abriendo la pantalla correspondiente.

## Cómo usarlo desde una pantalla
Cada pantalla conecta el botón de la barra superior con una línea:

    findViewById<View>(R.id.btnMenu).setOnClickListener { v -> MenuDesplegable.mostrar(this, v) }

La barra superior (`barra_superior.xml`) ya incluye el botón `btnMenu`.

## Qué NO va aquí
Las pantallas de destino (cada una va en su propia carpeta de `ui/`) ni lógica de juego.