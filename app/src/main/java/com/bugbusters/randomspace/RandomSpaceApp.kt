package com.bugbusters.randomspace

import android.app.Application
import com.bugbusters.randomspace.data.local.RandomSpaceDao
import com.bugbusters.randomspace.data.local.RandomSpaceDbHelper
import com.bugbusters.randomspace.data.repository.RandomSpaceRepository
import android.app.Activity
import android.os.Bundle
import com.bugbusters.randomspace.audio.ControlMusica
import com.bugbusters.randomspace.data.remote.ClienteFirebase
import com.bugbusters.randomspace.data.repository.SesionRepository
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Retrofit

/**
 * Aplicación: crea una sola vez la base de datos y el repositorio que comparten todas las pantallas.
 * Se registra en AndroidManifest.xml (android:name) y cada pantalla lo obtiene con
 * `(application as RandomSpaceApp).repositorio`.
 */
class RandomSpaceApp : Application() {

    // by lazy: se crea la primera vez que alguien lo pide, y solo una vez
    val repositorio: RandomSpaceRepository by lazy {
        RandomSpaceRepository(RandomSpaceDao(RandomSpaceDbHelper(this))) { sesion.sesionActual()?.uid }
    }

    // Quién ha iniciado sesión y su token
    val sesion: SesionRepository by lazy { SesionRepository(FirebaseAuth.getInstance()) }

    // Retrofit compartido: los repositorios del top 10 y del bote crean sus llamadas a partir de él
    val retrofit: Retrofit by lazy { ClienteFirebase.crear() }

    override fun onCreate() {
        super.onCreate()
        // Cuenta las pantallas visibles: con la primera suena la música y sin ninguna (app al fondo) se para
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var visibles = 0

            override fun onActivityStarted(activity: Activity) {
                if (visibles++ == 0) ControlMusica.arrancar(this@RandomSpaceApp)
            }

            override fun onActivityStopped(activity: Activity) {
                // Al girar el móvil la pantalla se recrea: no hace falta cortar la música
                if (--visibles == 0 && !activity.isChangingConfigurations) ControlMusica.parar(this@RandomSpaceApp)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}

