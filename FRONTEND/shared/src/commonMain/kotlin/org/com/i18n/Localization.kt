package org.com.i18n

import androidx.compose.runtime.*

val LocalRoomifyStrings = staticCompositionLocalOf<RoomifyStrings> { EnStrings }

@Composable
fun RoomifyLocalization(
    content: @Composable () -> Unit
) {
    val persistence = rememberLocalizationPersistence()
    var language by remember { 
        mutableStateOf(Language.fromCode(persistence.getLanguage() ?: "en")) 
    }

    val strings = remember(language) {
        when (language) {
            Language.ENGLISH -> EnStrings
            Language.SWAHILI -> SwStrings
        }
    }

    val localizationManager = remember {
        object : LocalizationManager {
            override val currentLanguage: Language
                get() = language

            override fun changeLanguage(newLanguage: Language) {
                language = newLanguage
                persistence.saveLanguage(newLanguage.code)
            }
        }
    }

    CompositionLocalProvider(
        LocalRoomifyStrings provides strings,
        LocalLocalizationManager provides localizationManager
    ) {
        content()
    }
}

interface LocalizationManager {
    val currentLanguage: Language
    fun changeLanguage(newLanguage: Language)
}

val LocalLocalizationManager = staticCompositionLocalOf<LocalizationManager> {
    object : LocalizationManager {
        override val currentLanguage: Language = Language.ENGLISH
        override fun changeLanguage(newLanguage: Language) {}
    }
}
