package org.com.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AndroidLocalizationPersistence(private val context: Context) : LocalizationPersistence {
    private val prefs = context.getSharedPreferences("roomify_prefs", Context.MODE_PRIVATE)

    override fun saveLanguage(code: String) {
        prefs.edit().putString("language", code).apply()
    }

    override fun getLanguage(): String? {
        return prefs.getString("language", "en")
    }
}

@Composable
actual fun rememberLocalizationPersistence(): LocalizationPersistence {
    val context = LocalContext.current
    return remember(context) {
        AndroidLocalizationPersistence(context)
    }
}
