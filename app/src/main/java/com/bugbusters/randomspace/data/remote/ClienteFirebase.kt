package com.bugbusters.randomspace.data.remote

import com.google.firebase.FirebaseApp
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/** Retrofit preparado para la API REST de la Realtime Database. */
object ClienteFirebase {

    fun crear(): Retrofit {
        // La dirección sale de google-services.json: así no se escribe en el código ni se sube a GitHub
        val direccion = checkNotNull(FirebaseApp.getInstance().options.databaseUrl) {
            "google-services.json no incluye la dirección de la base de datos"
        }
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())   // Moshi con reflexión
            .build()

        return Retrofit.Builder()
            .baseUrl(direccion.trimEnd('/') + "/")   // Retrofit exige que termine en /
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            // Las respuestas llegan como Single y las peticiones van en segundo plano
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }
}