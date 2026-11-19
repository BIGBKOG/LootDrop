package com.example.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.SteamGameEntity
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.components.buildGiveawayImageRequest
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.util.LocalLowEndOptimization
import java.util.Locale

@Composable
internal fun HomeRecentSteamGamesSection(
    games: List<SteamGameEntity>,
    steamUserId: String,
    isSyncingSteam: Boolean,
    onOpenSteamDialog: () -> Unit,
    onQuickSteamSync: () -> Unit,
    onOpenAchievements: (SteamGameEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val lang = s.language
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_recent_steam_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(NeonCyan.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(13.dp))
                    }
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "ACTIVIDAD RECIENTE EN STEAM"
                            AppLanguage.FRENCH -> "ACTIVITÉ RÉCENTE STEAM"
                            AppLanguage.ENGLISH -> "RECENT STEAM ACTIVITY"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = when (lang) {
                        AppLanguage.SPANISH -> "Tus 3 juegos jugados recientemente y logros"
                        AppLanguage.FRENCH -> "Vos 3 jeux joués récemment et succès"
                        AppLanguage.ENGLISH -> "Your 3 most recent played games & achievements"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (steamUserId.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSyncingSteam) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan, strokeWidth = 2.dp)
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { onQuickSteamSync() }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .testTag("home_steam_quick_sync_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = s.library.steamSyncCd, tint = NeonCyan, modifier = Modifier.size(13.dp))
                                Text(
                                    when (lang) {
                                        AppLanguage.SPANISH -> "Sincr."
                                        AppLanguage.FRENCH -> "Synchro"
                                        AppLanguage.ENGLISH -> "Sync"
                                    },
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        if (games.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("home_steam_connect_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(46.dp).clip(CircleShape).background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                    }
                    Text(
                        text = if (steamUserId.isBlank()) {
                            when (lang) {
                                AppLanguage.SPANISH -> "Conecta tu Cuenta de Steam"
                                AppLanguage.FRENCH -> "Connectez Votre Compte Steam"
                                AppLanguage.ENGLISH -> "Connect Your Steam Account"
                            }
                        } else {
                            when (lang) {
                                AppLanguage.SPANISH -> "Sin Juegos de Steam Sincronizados"
                                AppLanguage.FRENCH -> "Aucun Jeu Steam Synchronisé"
                                AppLanguage.ENGLISH -> "No Steam Games Synced"
                            }
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (steamUserId.isBlank()) {
                            s.home.homeRecentSteamEmpty
                        } else {
                            when (lang) {
                                AppLanguage.SPANISH -> "Sincroniza tu biblioteca de Steam para importar horas jugadas, títulos recientes y logros."
                                AppLanguage.FRENCH -> "Synchronisez votre bibliothèque Steam pour importer vos heures de jeu, titres récents et succès."
                                AppLanguage.ENGLISH -> "Sync your Steam library to import your playtime, recently played games, and achievements."
                            }
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Button(
                        onClick = onOpenSteamDialog,
                        modifier = Modifier.height(44.dp).testTag("home_connect_steam_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (steamUserId.isBlank()) {
                                    when (lang) {
                                        AppLanguage.SPANISH -> "Conectar ID de Steam"
                                        AppLanguage.FRENCH -> "Connecter ID Steam"
                                        AppLanguage.ENGLISH -> "Connect Steam ID"
                                    }
                                } else {
                                    when (lang) {
                                        AppLanguage.SPANISH -> "Gestionar Steam"
                                        AppLanguage.FRENCH -> "Gérer Steam"
                                        AppLanguage.ENGLISH -> "Manage Steam Sync"
                                    }
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                games.forEach { game ->
                    HomeRecentSteamGameCard(game = game, onOpenAchievements = onOpenAchievements)
                }
            }
        }
    }
}

@Composable
private fun HomeRecentSteamGameCard(
    game: SteamGameEntity,
    onOpenAchievements: (SteamGameEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val lang = s.language
    val lowEnd = LocalLowEndOptimization.current
    val headerImageRequest = remember(context, game.headerImageUrl, lowEnd) {
        buildGiveawayImageRequest(
            context = context,
            url = game.headerImageUrl,
            isLowEndOptimization = lowEnd,
            width = 320,
            height = 180
        )
    }

    Card(
        modifier = modifier.fillMaxWidth().testTag("home_recent_steam_card_${game.appId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                ) {
                    AsyncImage(
                        model = headerImageRequest,
                        contentDescription = game.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = game.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(13.dp))
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "${game.playtimeHours} jugadas"
                                AppLanguage.FRENCH -> "${game.playtimeHours} jouées"
                                AppLanguage.ENGLISH -> "${game.playtimeHours} total played"
                            },
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NeonCyan
                        )
                    }
                    if (game.playtime2WeeksMinutes > 0) {
                        val recentHours = game.playtime2WeeksMinutes / 60.0
                        val formattedRecent = if (recentHours >= 10.0) {
                            String.format(Locale.US, "%.0fh", recentHours)
                        } else {
                            String.format(Locale.US, "%.1fh", recentHours)
                        }
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "$formattedRecent en las últimas 2 sem."
                                AppLanguage.FRENCH -> "$formattedRecent ces 2 dern. sem."
                                AppLanguage.ENGLISH -> "$formattedRecent past 2 weeks"
                            },
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable {
                            val targetUri = try {
                                Intent(Intent.ACTION_VIEW, Uri.parse(game.launchUri)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            } catch (_: Exception) { null }
                            try {
                                if (targetUri != null) context.startActivity(targetUri)
                            } catch (_: Exception) {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(game.storeUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Launch ${game.name}", tint = NeonGreen, modifier = Modifier.size(18.dp))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (game.achievementsTotal > 0) {
                    val accent = if (game.achievementsPercentage == 100) NeonGreen else NeonCyan
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenAchievements(game) }
                            .testTag("home_steam_achievements_${game.appId}")
                            .padding(2.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                                Text(
                                    text = when (lang) {
                                        AppLanguage.SPANISH -> "Logros: ${game.achievementsUnlocked}/${game.achievementsTotal}"
                                        AppLanguage.FRENCH -> "Succès : ${game.achievementsUnlocked}/${game.achievementsTotal}"
                                        AppLanguage.ENGLISH -> "Achievements: ${game.achievementsUnlocked}/${game.achievementsTotal}"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    when (lang) {
                                        AppLanguage.SPANISH -> "• Ver Guías ➔"
                                        AppLanguage.FRENCH -> "• Voir Guides ➔"
                                        AppLanguage.ENGLISH -> "• View Guides ➔"
                                    },
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan
                                )
                            }
                            Text("${game.achievementsPercentage}%", fontSize = 11.sp, fontWeight = FontWeight.Black, color = accent)
                        }

                        LinearProgressIndicator(
                            progress = { (game.achievementsUnlocked.toFloat() / game.achievementsTotal.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                            color = accent,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("home_steam_no_achievements_${game.appId}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "Sin logros de Steam para este título"
                                    AppLanguage.FRENCH -> "Aucun succès Steam pour ce titre"
                                    AppLanguage.ENGLISH -> "No Steam Achievements for this title"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = when (lang) {
                                AppLanguage.SPANISH -> "Sin sincr."
                                AppLanguage.FRENCH -> "Non synchro"
                                AppLanguage.ENGLISH -> "Unsynced"
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
