package com.bugbusters.randomspace

import android.app.Application
import com.bugbusters.randomspace.data.local.RandomSpaceDao
import com.bugbusters.randomspace.data.local.RandomSpaceDbHelper
import com.bugbusters.randomspace.data.repository.RandomSpaceRepository

/**
 * Aplicación: crea una sola vez la base de datos y el repositorio que comparten todas las pantallas.
 * Se registra en AndroidManifest.xml (android:name) y cada pantalla lo obtiene con
 * `(application as RandomSpaceApp).repositorio`.
 */
class RandomSpaceApp : Application() {

    // by lazy: se crea la primera vez que alguien lo pide, y solo una vez
    val repositorio: RandomSpaceRepository by lazy {
        RandomSpaceRepository(RandomSpaceDao(RandomSpaceDbHelper(this)))
    }
}
