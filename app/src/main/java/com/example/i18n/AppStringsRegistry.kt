package com.example.i18n

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.preferences.AppPreferences

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { StringsEn }

object AppStringsRegistry {
    fun forLanguage(language: AppLanguage): AppStrings {
        return when (language) {
            AppLanguage.ENGLISH -> StringsEn
            AppLanguage.SPANISH -> StringsEs
        }
    }

    fun forContext(context: Context): AppStrings {
        val lang = runCatching { AppPreferences(context).appLanguage }.getOrDefault(AppLanguage.ENGLISH)
        LocaleAssetCatalog.preload(context, lang)
        return forLanguage(lang)
    }
}
