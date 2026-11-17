package com.example.i18n

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object LocaleSyncHelper {

    fun localizedContext(base: Context, language: AppLanguage): Context {
        val locale = language.locale
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocales(LocaleList(locale))
        return base.createConfigurationContext(config)
    }

    fun localizedContextFromPrefs(base: Context): Context {
        val prefs = com.example.preferences.AppPreferences(base)
        return localizedContext(base, prefs.appLanguage)
    }

    fun applyDefaultLocale(language: AppLanguage) {
        Locale.setDefault(language.locale)
    }

    fun applyAndRefreshSystemSurfaces(context: Context, language: AppLanguage) {
        applyDefaultLocale(language)
        runCatching { com.example.notifications.NotificationHelper(context) }
        runCatching { com.example.shortcuts.AppShortcutHelper.updateDynamicShortcuts(context) }
        runCatching { com.example.widget.WidgetUpdateHelper.updateAllWidgets(context) }
    }
}
