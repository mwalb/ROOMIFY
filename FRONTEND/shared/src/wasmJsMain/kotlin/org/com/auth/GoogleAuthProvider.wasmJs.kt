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

      const triggerPrompt = () => {
        if (!window.google?.accounts?.id) {
          console.error('Google Identity Services did not initialize.');
          onResult(null);
          return;
        }

        try {
          window.google.accounts.id.initialize({
            client_id: clientId,
            callback: (response) => {
              if (response && response.credential) {
                onResult(response.credential);
              } else {
                onResult(null);
              }
            },
            auto_select: false,
            cancel_on_tap_outside: true
          });

          // First attempt OneTap prompt
          window.google.accounts.id.prompt((notification) => {
            if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
              console.warn('Google OneTap dismissed/not displayed:', notification.getNotDisplayedReason?.() || notification.getSkippedReason?.());
              
              // Fallback: If OneTap is suppressed or blocked by browser/origin, render hidden Google button and click it to open standard popup
              let btnContainer = document.getElementById('roomify-hidden-google-btn');
              if (!btnContainer) {
                btnContainer = document.createElement('div');
                btnContainer.id = 'roomify-hidden-google-btn';
                btnContainer.style.position = 'fixed';
                btnContainer.style.top = '-9999px';
                btnContainer.style.left = '-9999px';
                btnContainer.style.opacity = '0';
                btnContainer.style.pointerEvents = 'none';
                document.body.appendChild(btnContainer);
              }
              btnContainer.innerHTML = '';
              window.google.accounts.id.renderButton(btnContainer, {
                type: 'standard',
                theme: 'outline',
                size: 'large'
              });
              
              const innerBtn = btnContainer.querySelector('div[role="button"]') || btnContainer.querySelector('iframe') || btnContainer.firstElementChild;
              if (innerBtn) {
                innerBtn.click();
              } else {
                onResult(null);
              }
            }
          });
        } catch (e) {
          console.error('Google sign-in error:', e);
          onResult(null);
        }
      };

      if (window.google?.accounts?.id) {
        triggerPrompt();
        return;
      }

      let script = document.getElementById('google-identity-services');
      if (!script) {
        script = document.createElement('script');
        script.id = 'google-identity-services';
        script.src = 'https://accounts.google.com/gsi/client';
        script.async = true;
        script.onerror = () => {
          console.error('Failed to load Google Identity Services');
          onResult(null);
        };
        document.head.appendChild(script);
      }
      script.addEventListener('load', triggerPrompt, { once: true });
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
