package com.bugbusters.randomspace.ui.name

import android.app.Activity
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.bugbusters.randomspace.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.reactivex.rxjava3.core.Single
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.exceptions.ClearCredentialException

/** Abre el selector de cuentas de Google y devuelve el token de la cuenta elegida. */
class LoginGoogle(private val activity: Activity) {

    // Necesita la pantalla (Activity) porque el selector se dibuja encima de ella
    fun pedirToken(): Single<String> = Single.create { emisor ->
        // default_web_client_id lo genera Gradle a partir de google-services.json
        val opcion = GetSignInWithGoogleOption
            .Builder(activity.getString(R.string.default_web_client_id))
            .build()
        val peticion = GetCredentialRequest.Builder()
            .addCredentialOption(opcion)
            .build()

        // Si la pantalla se cierra mientras tanto, se cancela la petición
        val cancelacion = CancellationSignal()
        emisor.setCancellable { cancelacion.cancel() }

        CredentialManager.create(activity).getCredentialAsync(
            activity,
            peticion,
            cancelacion,
            ContextCompat.getMainExecutor(activity),
            object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {

                override fun onResult(result: GetCredentialResponse) {
                    val credencial = result.credential
                    if (credencial is CustomCredential &&
                        credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        try {
                            emisor.onSuccess(GoogleIdTokenCredential.createFrom(credencial.data).idToken)
                        } catch (error: Exception) {
                            emisor.tryOnError(error)
                        }
                    } else {
                        emisor.tryOnError(IllegalStateException("La cuenta elegida no es de Google"))
                    }
                }

                override fun onError(e: GetCredentialException) {
                    emisor.tryOnError(e)
                }
            }
        )
    }

    /** Para que la próxima vez Google vuelva a preguntar con qué cuenta entrar. */
    fun olvidarCuenta() {
        CredentialManager.create(activity).clearCredentialStateAsync(
            ClearCredentialStateRequest(),
            null,
            ContextCompat.getMainExecutor(activity),
            object : CredentialManagerCallback<Void?, ClearCredentialException> {
                override fun onResult(result: Void?) {}
                override fun onError(e: ClearCredentialException) {
                    Log.w("LoginGoogle", "No se pudo olvidar la cuenta", e)
                }
            }
        )
    }
}