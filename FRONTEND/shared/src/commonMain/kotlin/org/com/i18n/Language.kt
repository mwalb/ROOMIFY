package org.com.i18n

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SWAHILI("sw", "Kiswahili");

    companion object {
        fun fromCode(code: String): Language {
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}
