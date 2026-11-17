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
    );

    val locale: Locale
        get() = Locale(code)

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return when (code?.trim()?.lowercase()) {
                "es", "es-es", "es-mx", "es-419", "spanish", "español" -> SPANISH
                else -> ENGLISH
            }
        }
    }
}
