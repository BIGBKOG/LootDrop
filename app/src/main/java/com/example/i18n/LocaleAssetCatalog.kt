package com.example.i18n

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads per-language enum, theme, challenge, and milestone text catalogs from
 * Android asset files (`assets/locales/{lang}.json`) so English and Spanish strings
 * are maintained in separate language files rather than mixed inline branches.
 */
object LocaleAssetCatalog {

    private val cache = ConcurrentHashMap<AppLanguage, JSONObject>()

    @Volatile
    private var appContext: Context? = null

    fun preload(context: Context, language: AppLanguage): JSONObject? {
        val appCtx = context.applicationContext ?: context
        appContext = appCtx
        return getOrLoad(appCtx, language)
    }

    fun getOrLoad(context: Context?, language: AppLanguage): JSONObject? {
        cache[language]?.let { return it }

        val ctx = context ?: appContext
        if (ctx != null) {
            runCatching {
                ctx.assets.open("locales/${language.code}.json").bufferedReader(Charsets.UTF_8).use { reader ->
                    val parsed = JSONObject(reader.readText())
                    cache[language] = parsed
                    return parsed
                }
            }
        }

        // Pure-JVM / unit test fallback reading straight from module assets on disk
        val candidatePaths = listOf(
            "app/src/main/assets/locales/${language.code}.json",
            "src/main/assets/locales/${language.code}.json"
        )
        for (path in candidatePaths) {
            val file = File(path)
            if (file.isFile) {
                runCatching {
                    val parsed = JSONObject(file.readText(Charsets.UTF_8))
                    cache[language] = parsed
                    return parsed
                }
            }
        }

        return null
    }

    fun getField(
        language: AppLanguage,
        section: String,
        key: String,
        field: String,
        fallback: String
    ): String {
        val root = getOrLoad(appContext, language) ?: return fallback
        val value = root.optJSONObject(section)
            ?.optJSONObject(key)
            ?.optString(field, "")
            .orEmpty()
        return value.ifEmpty { fallback }
    }

    fun getPrefixMatchedField(
        language: AppLanguage,
        section: String,
        id: String,
        field: String,
        fallback: String
    ): String {
        val root = getOrLoad(appContext, language) ?: return fallback
        val sectionObj = root.optJSONObject(section) ?: return fallback
        val keys = sectionObj.keys()
        while (keys.hasNext()) {
            val prefix = keys.next()
            if (id.startsWith(prefix)) {
                val value = sectionObj.optJSONObject(prefix)?.optString(field, "").orEmpty()
                if (value.isNotEmpty()) return value
            }
        }
        return fallback
    }

    fun clearCacheForTesting() {
        cache.clear()
    }
}
