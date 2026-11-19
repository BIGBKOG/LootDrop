package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted

@Composable
internal fun LiveNotificationPreviewCard(
    isNotificationsEnabled: Boolean,
    isRichArtEnabled: Boolean,
    isActionsEnabled: Boolean
) {
    val s = LocalAppStrings.current
    val lang = s.language
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "VISTA PREVIA EN VIVO"
                            AppLanguage.FRENCH -> "APERÇU EN DIRECT"
                            AppLanguage.ENGLISH -> "LIVE NOTIFICATION PREVIEW"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = if (isNotificationsEnabled) {
                        when (lang) {
                            AppLanguage.SPANISH -> "Así se verá en tu dispositivo"
                            AppLanguage.FRENCH -> "Voici l'aperçu sur votre appareil"
                            AppLanguage.ENGLISH -> "How it looks on your device"
                        }
                    } else {
                        when (lang) {
                            AppLanguage.SPANISH -> "Alertas en pausa"
                            AppLanguage.FRENCH -> "Notifications en pause"
                            AppLanguage.ENGLISH -> "Notifications paused"
                        }
                    },
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF161B26))
                    .border(1.dp, Color(0xFF28324A), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6C5CE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text("LootDrop", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("•", fontSize = 10.sp, color = TextMuted)
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "ahora"
                                AppLanguage.FRENCH -> "maintenant"
                                AppLanguage.ENGLISH -> "now"
                            },
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF6C5CE7).copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                when (lang) {
                                    AppLanguage.SPANISH -> "🔥 Juegos Destacados"
                                    AppLanguage.FRENCH -> "🔥 Grands jeux"
                                    AppLanguage.ENGLISH -> "🔥 Major Games"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB388FF)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = when (lang) {
                                AppLanguage.SPANISH -> "🎮 GRATIS AHORA en Epic Games: Death Stranding"
                                AppLanguage.FRENCH -> "🎮 GRATUIT sur Epic Games : Death Stranding"
                                AppLanguage.ENGLISH -> "🎮 FREE NOW on Epic Games: Death Stranding"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = when (lang) {
                                AppLanguage.SPANISH -> "Valorado en $39.99 • ¡Reclámalo para siempre! Toca para ver."
                                AppLanguage.FRENCH -> "Valeur 39,99 $ • À garder à vie ! Touchez pour voir les détails."
                                AppLanguage.ENGLISH -> "Worth $39.99 • Claim it to keep forever! Tap to view details."
                            },
                            fontSize = 11.sp,
                            color = Color(0xFFB0BAC9),
                            lineHeight = 15.sp
                        )
                    }

                    if (isRichArtEnabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1E283C), Color(0xFF0F172A), Color(0xFF2C1654))
                                    )
                                )
                                .border(1.dp, Color(0xFF384566), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonGreen)
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        when (lang) {
                                            AppLanguage.SPANISH -> "100% GRATIS"
                                            AppLanguage.FRENCH -> "100 % GRATUIT"
                                            AppLanguage.ENGLISH -> "100% OFF"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0B0F19)
                                    )
                                }
                                Text(
                                    text = "Death Stranding: Director's Cut • PC",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (isActionsEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SimulatedActionButton(label = s.system.notifActionClaimFree, color = NeonGreen)
                            SimulatedActionButton(label = s.system.notifActionBookmark, color = Color(0xFF38BDF8))
                            SimulatedActionButton(label = s.system.notifActionShare, color = Color(0xFFD946EF))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NotificationStatusChip(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                0.5.dp,
                if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SimulatedActionButton(
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.8.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
