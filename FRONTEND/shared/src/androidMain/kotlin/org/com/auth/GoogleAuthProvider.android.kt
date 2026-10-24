package org.com.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

private const val FALLBACK_WEB_CLIENT_ID = "1041914574744-evaa3p91lu43jr5crr42esqrivkha66m.apps.googleusercontent.com"

@Composable
actual fun rememberGoogleSignInLauncher(): GoogleSignInLauncher {
    val context = LocalContext.current
    var callbackState by remember { mutableStateOf<((String?) -> Unit)?>(null) }
    val webClientIdResource = remember(context) {
        context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    }
    val webClientId = remember(context, webClientIdResource) {
        val id = if (webClientIdResource == 0) "" else context.getString(webClientIdResource)
        if (id.isBlank()) FALLBACK_WEB_CLIENT_ID else id
    }
    val signInClient = remember(context, webClientId) {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(webClientId)
        GoogleSignIn.getClient(context, options.build())
    }

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val callback = callbackState ?: return@rememberLauncherForActivityResult
        if (result.resultCode != Activity.RESULT_OK || result.data == null) {
            callback(null)
            return@rememberLauncherForActivityResult
        }

        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            callback(account.idToken)
        } catch (e: ApiException) {
            Log.e("RoomifyGoogleSignIn", "Google sign-in status: ${e.statusCode}", e)
            callback(null)
        }
    }

    return remember(signInClient, activityResultLauncher) {
        GoogleSignInLauncher { onResult ->
            callbackState = onResult
            activityResultLauncher.launch(signInClient.signInIntent)
        }
    }
}
