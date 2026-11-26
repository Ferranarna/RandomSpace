package com.bugbusters.randomspace.data.repository

import com.bugbusters.randomspace.model.Sesion
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.core.Single
import com.google.firebase.auth.GoogleAuthProvider

/** Puerta de entrada a Firebase Authentication para el resto de la app. */
class SesionRepository(private val auth: FirebaseAuth) {

    /** La sesión actual, o null si nadie ha iniciado sesión. Firebase la recuerda al cerrar la app. */
    fun sesionActual(): Sesion? =
        auth.currentUser?.let { Sesion(it.uid, it.displayName.orEmpty()) }

    /** Inicia sesión en Firebase con el token que ha dado Google. */
    fun iniciarSesion(tokenGoogle: String): Single<Sesion> = Single.create { emisor ->
        auth.signInWithCredential(GoogleAuthProvider.getCredential(tokenGoogle, null))
            .addOnSuccessListener {
                val nueva = sesionActual()
                if (nueva != null) emisor.onSuccess(nueva)
                else emisor.tryOnError(IllegalStateException("Firebase no ha abierto la sesión"))
            }
            .addOnFailureListener { error -> emisor.tryOnError(error) }
    }

    /**
     * Token para las llamadas a la base de datos. Hay que pedirlo antes de cada llamada porque caduca;
     * Firebase devuelve el guardado y solo pide uno nuevo si hace falta.
     */
    fun token(): Single<String> = Single.create { emisor ->
        val usuario = auth.currentUser
        if (usuario == null) {
            emisor.tryOnError(IllegalStateException("No hay sesión iniciada"))
            return@create
        }
        usuario.getIdToken(false)
            .addOnSuccessListener { resultado ->
                val token = resultado.token
                if (token != null) emisor.onSuccess(token)
                else emisor.tryOnError(IllegalStateException("Firebase no ha devuelto el token"))
            }
            .addOnFailureListener { error -> emisor.tryOnError(error) }
    }
}