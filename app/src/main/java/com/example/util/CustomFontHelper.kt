package com.example.util

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface as ComposeTypeface
import com.example.i18n.AppLanguage
import com.example.preferences.AppPreferences
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object CustomFontHelper {

    private const val FONT_DIR_NAME = "custom_fonts"
    private const val MAX_FONT_BYTES = 20L * 1024L * 1024L // 20 MB limit

    sealed class ImportResult {
        data class Success(val filePath: String, val displayName: String) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    fun getFontDir(context: Context): File {
        return File(context.filesDir, FONT_DIR_NAME).apply {
            if (!exists()) mkdirs()
        }
    }

    fun importFontFromUri(context: Context, uri: Uri): ImportResult {
        val isEs = AppPreferences(context).appLanguage == AppLanguage.SPANISH
        val displayName = resolveDisplayName(context, uri)
        val lowerName = displayName.lowercase()

        if (lowerName.endsWith(".woff") || lowerName.endsWith(".woff2")) {
            return ImportResult.Error(
                if (isEs) "Las fuentes web WOFF/WOFF2 no son compatibles en Android. Importa un archivo .ttf u .otf."
                else "WOFF/WOFF2 web fonts are not supported on Android. Please import a .ttf or .otf font file."
            )
        }

        val extension = when {
            lowerName.endsWith(".otf") -> "otf"
            lowerName.endsWith(".ttc") -> "ttc"
            else -> "ttf"
        }

        val fontDir = getFontDir(context)
        val tempFile = File(fontDir, "pending_import_${System.currentTimeMillis()}.$extension")

        return try {
            val input = context.contentResolver.openInputStream(uri)
                ?: return ImportResult.Error(
                    if (isEs) "No se pudo leer el archivo de fuente seleccionado."
                    else "Could not read the selected font file."
                )

            var totalBytes = 0L
            input.use { inStream ->
                FileOutputStream(tempFile).use { outStream ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val read = inStream.read(buffer)
                        if (read <= 0) break
                        totalBytes += read
                        if (totalBytes > MAX_FONT_BYTES) {
                            tempFile.delete()
                            return ImportResult.Error(
                                if (isEs) "El archivo de fuente es demasiado grande (máx. 20 MB)."
                                else "Font file is too large (max 20 MB)."
                            )
                        }
                        outStream.write(buffer, 0, read)
                    }
                    outStream.flush()
                }
            }

            if (totalBytes < 12L || !hasValidFontHeader(tempFile)) {
                tempFile.delete()
                return ImportResult.Error(
                    if (isEs) "Archivo inválido. Selecciona una fuente .ttf u .otf válida."
                    else "Invalid font file. Please select a valid .ttf or .otf font."
                )
            }

            val parsedTypeface = runCatching { Typeface.createFromFile(tempFile) }.getOrNull()
            if (parsedTypeface == null) {
                tempFile.delete()
                return ImportResult.Error(
                    if (isEs) "Android no pudo leer esta fuente. Prueba con otro archivo .ttf u .otf."
                    else "Android could not parse this font file. Try another .ttf or .otf font."
                )
            }

            // Remove old imported fonts so storage stays clean
            fontDir.listFiles()?.forEach { existing ->
                if (existing.absolutePath != tempFile.absolutePath) {
                    existing.delete()
                }
            }

            val finalFile = File(fontDir, "custom_app_font_${System.currentTimeMillis()}.$extension")
            if (!tempFile.renameTo(finalFile)) {
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }

            ImportResult.Success(
                filePath = finalFile.absolutePath,
                displayName = displayName
            )
        } catch (e: Exception) {
            tempFile.delete()
            ImportResult.Error(
                if (isEs) "Error al importar fuente: ${e.message ?: "Error desconocido"}"
                else "Failed to import font: ${e.message ?: "Unknown error"}"
            )
        }
    }

    fun loadComposeFontFamily(fontPath: String): FontFamily {
        if (fontPath.isBlank()) return FontFamily.Default
        val file = File(fontPath)
        if (!file.exists() || !file.isFile || !hasValidFontHeader(file)) {
            return FontFamily.Default
        }
        return runCatching {
            val nativeTypeface = Typeface.createFromFile(file)
            FontFamily(ComposeTypeface(nativeTypeface))
        }.getOrDefault(FontFamily.Default)
    }

    fun clearCustomFonts(context: Context) {
        runCatching {
            getFontDir(context).listFiles()?.forEach { it.delete() }
        }
    }

    fun hasValidFontHeader(file: File): Boolean {
        if (!file.exists() || file.length() < 12L) return false
        return runCatching {
            val header = ByteArray(4)
            FileInputStream(file).use { stream ->
                val read = stream.read(header)
                if (read < 4) return false
            }
            val b0 = header[0].toInt() and 0xFF
            val b1 = header[1].toInt() and 0xFF
            val b2 = header[2].toInt() and 0xFF
            val b3 = header[3].toInt() and 0xFF

            val isTrueType = (b0 == 0x00 && b1 == 0x01 && b2 == 0x00 && b3 == 0x00) ||
                (b0 == 't'.code && b1 == 'r'.code && b2 == 'u'.code && b3 == 'e'.code) ||
                (b0 == 't'.code && b1 == 'y'.code && b2 == 'p'.code && b3 == '1'.code)
            val isOpenType = (b0 == 'O'.code && b1 == 'T'.code && b2 == 'T'.code && b3 == 'O'.code)
            val isTrueTypeCollection = (b0 == 't'.code && b1 == 't'.code && b2 == 'c'.code && b3 == 'f'.code)

            isTrueType || isOpenType || isTrueTypeCollection
        }.getOrDefault(false)
    }

    private fun resolveDisplayName(context: Context, uri: Uri): String {
        runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        val name = cursor.getString(idx)?.trim()
                        if (!name.isNullOrBlank()) return name
                    }
                }
            }
        }
        val segment = uri.lastPathSegment?.substringAfterLast('/')?.trim()
        return if (!segment.isNullOrBlank()) segment else "custom_font.ttf"
    }
}
