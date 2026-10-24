package org.com.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (onResult) => {
      const clientId = document.querySelector('meta[name="google-signin-client_id"]')?.content;
      if (!clientId || clientId.startsWith('YOUR_')) {
        console.error('Roomify Google Sign-In requires a configured web client ID.');
        onResult(null);
        return;
      }

      const signIn = () => {
        if (!window.google?.accounts?.id) {
          console.error('Google Identity Services did not initialize.');
          onResult(null);
          return;
        }

        window.google.accounts.id.initialize({
          client_id: clientId,
          callback: (response) => onResult(response.credential || null)
        });
        window.google.accounts.id.prompt((notification) => {
          if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
            onResult(null);
          }
        });
      };

      if (window.google?.accounts?.id) {
        signIn();
        return;
      }

      let script = document.getElementById('google-identity-services');
      if (!script) {
        script = document.createElement('script');
        script.id = 'google-identity-services';
        script.src = 'https://accounts.google.com/gsi/client';
        script.async = true;
        script.onerror = () => onResult(null);
        document.head.appendChild(script);
      }
      script.addEventListener('load', signIn, { once: true });
    }
    """
)
private external fun requestGoogleIdToken(onResult: (String?) -> Unit)

@Composable
actual fun rememberGoogleSignInLauncher(): GoogleSignInLauncher {
    return remember {
        GoogleSignInLauncher { onResult ->
            requestGoogleIdToken(onResult)
        }
    }
}
