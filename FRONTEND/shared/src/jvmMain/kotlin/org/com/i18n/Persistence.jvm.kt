package org.com.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.prefs.Preferences

class JvmLocalizationPersistence : LocalizationPersistence {
    private val prefs = Preferences.userNodeForPackage(JvmLocalizationPersistence::class.java)

    override fun saveLanguage(code: String) {
        prefs.put("language", code)
    }

    override fun getLanguage(): String? {
        return prefs.get("language", "en")
    }
}

@Composable
actual fun rememberLocalizationPersistence(): LocalizationPersistence {
    return remember {
        JvmLocalizationPersistence()
    }
}
