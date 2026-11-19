package com.example.i18n

import java.util.Locale

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val shortBadge: String,
    val flagEmoji: String
) {
    ENGLISH(
        code = "en",
        nativeName = "English",
        shortBadge = "EN",
        flagEmoji = "🇺🇸"
    ),
    SPANISH(
        code = "es",
        nativeName = "Español",
        shortBadge = "ES",
        flagEmoji = "🇪🇸"
    ),
    FRENCH(
        code = "fr",
        nativeName = "Français",
        shortBadge = "FR",
        flagEmoji = "🇫🇷"
    );

    val locale: Locale
        get() = Locale(code)

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return when (code?.trim()?.lowercase()) {
                "es", "es-es", "es-mx", "es-419", "spanish", "español" -> SPANISH
                "fr", "fr-fr", "fr-ca", "fr-be", "fr-ch", "french", "français", "francais" -> FRENCH
                else -> ENGLISH
            }
        }
    }
}
