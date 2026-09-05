package org.com.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

class IosLocalizationPersistence : LocalizationPersistence {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun saveLanguage(code: String) {
        defaults.setObject(code, "language")
    }

    override fun getLanguage(): String? {
        return defaults.stringForKey("language")
    }
}

@Composable
actual fun rememberLocalizationPersistence(): LocalizationPersistence {
    return remember {
        IosLocalizationPersistence()
    }
}
