package org.com.i18n

import androidx.compose.runtime.Composable

interface LocalizationPersistence {
    fun saveLanguage(code: String)
    fun getLanguage(): String?
}

@Composable
expect fun rememberLocalizationPersistence(): LocalizationPersistence
