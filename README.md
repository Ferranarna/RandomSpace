# RandomSpace

Videojuego colaborativo de azar para Android, desarrollado con Android Studio y Kotlin.
Proyecto de la asignatura *Desarrollo de aplicaciones móviles* (FP.061, UOC).

Evoluciona en tres productos encadenados:

| Producto | Contenido | Entrega |
|---|---|---|
| P1 | Juego base, SQLite y RxJava | _(por confirmar)_ |
| P2 | Multimedia y funcionalidades Android | 30 nov 2026 |
| P3 | Multijugador con Firebase | 15 dic 2026 |

## Configuración del proyecto

| Ajuste | Valor |
|---|---|
| Plantilla | Empty Views Activity (Views/XML) |
| Lenguaje | Kotlin |
| Configuración de build | Kotlin DSL (`.kts`) |
| Nombre / paquete | `RandomSpace` / `com.bugbusters.randomspace` |
| minSdk | 24 (Android 7.0) |
| compileSdk / targetSdk | 37 |
| Android Gradle Plugin | 9.4.1 (ver `gradle/libs.versions.toml`) |
| JDK de Gradle | 25 (ver `gradle/gradle-daemon-jvm.properties`) |

## Requisitos

1. **Git**.
2. **Android Studio** (versión reciente) instalado en **Windows**, no en WSL. Incluye el JDK necesario.
3. En *Tools → SDK Manager → SDK Platforms*, tener instalada la plataforma **Android 17 (API 37)**, que es la que usa `compileSdk`, y la API 36 para el emulador.
4. Virtualización activada en la BIOS y Windows Hypervisor Platform activo, para el emulador.

## Primeros pasos

1. Clona el repositorio en una ruta **corta, sin tildes ni espacios y fuera de OneDrive**, por ejemplo `C:\Programacio\git\RandomSpace`.
2. En Android Studio: *Open* y selecciona la carpeta del proyecto.
3. Espera al *Gradle sync* (la primera vez descarga Gradle y las dependencias).
4. Crea el emulador (siguiente apartado) y pulsa ▶ *Run 'app'*.

## Emulador de referencia

Para que todos probemos en las mismas condiciones, usad esta configuración en *Device Manager → Create Device*:

| Opción | Valor |
|---|---|
| Dispositivo | Pixel 10 |
| Imagen del sistema | API 36.1 "Baklava" (Android 16) |
| Services | **Google Play Store** |
| Arquitectura | x86_64 (Intel/AMD) |
| Descarga | ≈ 1,9 GB |

Notas:
- Se elige **Google Play Store** porque en P3 el login con Google (Firebase) necesita Google Play Services.
- Se elige la imagen estándar, no la "16 KB Page Size", porque no usamos código nativo.
- Se evita la API 37 (Android 17) como único emulador por ser demasiado reciente para las librerías del curso.
- En un PC con procesador ARM, elegid la imagen arm64.

## Estructura del repositorio

```
RandomSpace/
├── app/                  Módulo Android (código, recursos y tests)
├── gradle/               Wrapper de Gradle y catálogo de versiones
├── build.gradle.kts      Configuración común de Gradle
└── settings.gradle.kts   Módulos del proyecto
```

## Arquitectura del código

El código vive en `app/src/main/java/com/bugbusters/randomspace/` y se organiza **por capas**.
Cada carpeta contiene un `explicacion.md` que describe qué va dentro y qué tipo de archivos.

```
com/bugbusters/randomspace/
├── ui/                  Interfaz: lo que el jugador ve y toca
│   ├── welcome/         Pantalla de bienvenida y mejores partidas
│   └── game/            Pantalla de la partida (Canvas y animaciones)
├── game/                Lógica del juego de azar (sin dependencias de Android)
├── model/               Clases de datos: jugador, partida, puntuación
└── data/                Guardar y recuperar información
    ├── local/           SQLite
    └── repository/      Puerta de entrada a los datos (RxJava)
```

Los recursos (`app/src/main/res/`) siguen la estructura estándar de Android: `layout/`, `drawable/`, `values/` (textos, colores, temas), `mipmap-*/` (icono) y, cuando haga falta, `menu/`.
Los textos visibles van siempre en `strings.xml`, nunca escritos en el código o el layout.

### Regla de dependencias

```
ui  ──►  game
 │        │
 └──►  data/repository  ──►  data/local
 │        │
 └────────┴──►  model
```

`ui` puede usar `game`, `data` y `model`. `game` y `data` pueden usar `model`. **Nunca al revés**: `model` no conoce a nadie, y `game` no importa nada de `android.*`.

### Por qué por capas

- **Responsabilidad única**: cada carpeta hace una sola cosa. Si falla el guardado de partidas se mira en `data/`; si falla una regla, en `game/`; si falla un dibujo, en `ui/`.
- **Lógica fácil de ampliar y de probar**: al no depender de Android, `game/` se prueba con tests unitarios sin emulador y se le pueden añadir reglas sin tocar la interfaz.
- **Base de datos aislada**: la UI solo habla con el repositorio, que decide los hilos con RxJava. Así no se bloquea el hilo principal.
- **Trabajo en equipo**: cada persona puede trabajar en una capa distinta con menos conflictos de Git.
- **Se explica bien en el UML y en la memoria**: las capas se corresponden con los paquetes del diagrama de clases.

Se descarta organizar por funcionalidad (`welcome/`, `ranking/`… cada una con su UI, datos y lógica) porque el proyecto es pequeño y crece **añadiendo capas**, no módulos independientes.

### Cómo se ampliará

Cada producto añade carpetas nuevas sin mover las existentes:

| Producto | Se añade | Para qué |
|---|---|---|
| P2 | `service/` | Música de fondo y gestión del foco de audio |
| P2 | `util/` | Permisos, calendario, notificaciones, capturas, ubicación e idioma |
| P2 | `ui/help/`, `ui/settings/` | WebView de ayuda; ajustes de música e idioma |
| P3 | `remote/` | Retrofit + Moshi y Firebase (login, top ten, premio común) |
| P3 | `ui/ranking/` | Pantalla del top ten y login con Google |

El `repository` es el punto clave: en P3 combinará `data/local/` con `remote/`, y la UI no tendrá que cambiar.

## Qué no se sube al repositorio

`build/`, `.gradle/`, `.idea/`, `local.properties`, keystores (`*.jks`, `*.keystore`) y `google-services.json`.
Cada miembro tiene su propio `local.properties` con la ruta de su SDK.

## Equipo

_(pendiente)_
