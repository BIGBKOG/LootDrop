package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.GiveawaysViewModel

@Composable
internal fun CustomFontSettingsCard(
    customFontPath: String,
    customFontName: String,
    viewModel: GiveawaysViewModel,
    context: Context
) {
    val s = LocalAppStrings.current
    val lang = s.language
    var isImportingFont by remember { mutableStateOf(false) }
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isImportingFont = true
            viewModel.importCustomFont(context, uri) { _, message ->
                isImportingFont = false
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val hasCustomFont = customFontPath.isNotBlank()
    SectionHeader(title = s.settings.appearanceFontSection, icon = Icons.Default.TextFields)
    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("custom_font_settings_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FontDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (hasCustomFont) {
                            customFontName.ifBlank {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Fuente Personalizada"
                                    AppLanguage.FRENCH -> "Police personnalisée"
                                    AppLanguage.ENGLISH -> "Custom Font"
                                }
                            }
                        } else {
                            when (lang) {
                                AppLanguage.SPANISH -> "Fuente del Sistema"
                                AppLanguage.FRENCH -> "Police du système"
                                AppLanguage.ENGLISH -> "System Default Font"
                            }
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (hasCustomFont) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                                .border(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                when (lang) {
                                    AppLanguage.SPANISH -> "PROPIA"
                                    AppLanguage.FRENCH -> "PERSO"
                                    AppLanguage.ENGLISH -> "CUSTOM"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Text(
                    text = if (hasCustomFont) {
                        when (lang) {
                            AppLanguage.SPANISH -> "Aplicada en todas las pantallas, tarjetas y menús"
                            AppLanguage.FRENCH -> "Appliquée sur tous les écrans, cartes et menus"
                            AppLanguage.ENGLISH -> "Applied across all screens, cards & menus"
                        }
                    } else {
                        when (lang) {
                            AppLanguage.SPANISH -> "Importa un archivo .ttf o .otf para personalizar textos"
                            AppLanguage.FRENCH -> "Importez un fichier .ttf ou .otf pour personnaliser"
                            AppLanguage.ENGLISH -> "Import a .ttf or .otf file to customize all text"
                        }
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text("Aa Bb Cc 123 • LootDrop", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                when (lang) {
                    AppLanguage.SPANISH -> "El veloz zorro marrón reclama juegos gratis de PC y Steam."
                    AppLanguage.FRENCH -> "Portez ce vieux whisky au juge blond qui fume sur Steam."
                    AppLanguage.ENGLISH -> "The quick brown fox claims free PC & Steam drops."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    fontPickerLauncher.launch(
                        arrayOf(
                            "font/ttf", "font/otf", "font/sfnt",
                            "application/x-font-ttf", "application/x-font-opentype",
                            "application/font-sfnt", "application/octet-stream", "*/*"
                        )
                    )
                },
                enabled = !isImportingFont,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("btn_import_custom_font")
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        isImportingFont -> when (lang) {
                            AppLanguage.SPANISH -> "Importando..."
                            AppLanguage.FRENCH -> "Importation..."
                            AppLanguage.ENGLISH -> "Importing..."
                        }
                        hasCustomFont -> when (lang) {
                            AppLanguage.SPANISH -> "Cambiar Fuente"
                            AppLanguage.FRENCH -> "Changer de police"
                            AppLanguage.ENGLISH -> "Change Font"
                        }
                        else -> s.settings.appearanceFontImportBtn
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (hasCustomFont) {
                OutlinedButton(
                    onClick = {
                        viewModel.resetCustomFont(context)
                        Toast.makeText(
                            context,
                            when (lang) {
                                AppLanguage.SPANISH -> "Fuente predeterminada restaurada"
                                AppLanguage.FRENCH -> "Police par défaut restaurée"
                                AppLanguage.ENGLISH -> "Restored default system font"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_reset_custom_font")
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = s.settings.appearanceFontResetBtn, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        when (lang) {
                            AppLanguage.SPANISH -> "Restablecer"
                            AppLanguage.FRENCH -> "Réinitialiser"
                            AppLanguage.ENGLISH -> "Reset"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
