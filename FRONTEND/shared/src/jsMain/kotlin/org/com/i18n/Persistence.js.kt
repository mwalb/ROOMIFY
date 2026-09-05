package org.com.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window

class JsLocalizationPersistence : LocalizationPersistence {
    override fun saveLanguage(code: String) {
        window.localStorage.setItem("language", code)
    }

    override fun getLanguage(): String? {
        return window.localStorage.getItem("language")
    }
}

@Composable
actual fun rememberLocalizationPersistence(): LocalizationPersistence {
    return remember {
        JsLocalizationPersistence()
    }
}
