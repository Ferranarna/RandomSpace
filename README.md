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

_La organización interna de paquetes se documentará cuando el equipo la acuerde._

## Qué no se sube al repositorio

`build/`, `.gradle/`, `.idea/`, `local.properties`, keystores (`*.jks`, `*.keystore`) y `google-services.json`.
Cada miembro tiene su propio `local.properties` con la ruta de su SDK.

## Equipo

_(pendiente)_
