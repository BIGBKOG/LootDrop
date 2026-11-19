package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BacklogChallengeGrid
import com.example.data.model.BacklogChallengeTile
import com.example.data.model.BacklogMilestoneReward
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedDisplayName
import com.example.i18n.localizedTitle
import com.example.preferences.AccentTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.LocalLowEndOptimization

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BacklogChallengeGridDialog(
    grid: BacklogChallengeGrid,
    onDismiss: () -> Unit,
    onToggleManualTile: (Int) -> Boolean,
    onRefreshCheck: () -> Unit,
    onClaimMilestones: ((List<BacklogMilestoneReward>) -> Unit) -> Unit,
    onEquipTheme: (AccentTheme) -> Unit,
    onSelectWeek: ((String) -> Unit)? = null,
    onSelectMonth: ((String) -> Unit)? = onSelectWeek,
    modifier: Modifier = Modifier
) {
    val weekSelector = onSelectWeek ?: onSelectMonth
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val lang = s.language
    val lowEnd = LocalLowEndOptimization.current
    var selectedTile by remember { mutableStateOf<BacklogChallengeTile?>(null) }
    var showHowItWorks by remember { mutableStateOf(false) }

    val glowAlpha = if (grid.isBlackout && !lowEnd) {
        val infiniteTransition = rememberInfiniteTransition(label = "blackout_glow")
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_pulse"
        ).value
    } else {
        0.75f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = if (grid.isBlackout) 2.dp else 1.dp,
                    brush = if (grid.isBlackout) {
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFFFFD700).copy(alpha = glowAlpha),
                                Color(0xFFE040FB).copy(alpha = glowAlpha),
                                Color(0xFF00E5FF).copy(alpha = glowAlpha),
                                Color(0xFFFFD700).copy(alpha = glowAlpha)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFF59E0B).copy(alpha = 0.7f),
                                Color(0xFF8B5CF6).copy(alpha = 0.4f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("dialog_backlog_challenge_grid"),
            color = Color(0xFF0B101D)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🎯 ${grid.weekTitle}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            if (grid.isBlackout) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                                            )
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.SPANISH -> "¡CARTÓN LLENO!"
                                            AppLanguage.FRENCH -> "CARTON PLEIN !"
                                            AppLanguage.ENGLISH -> "BLACKOUT!"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (grid.isCurrentWeek) {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Se renueva cada lunes • ⏳ Quedan ${grid.daysRemaining} días"
                                    AppLanguage.FRENCH -> "Renouvelé chaque lundi • ⏳ ${grid.daysRemaining} j restants"
                                    AppLanguage.ENGLISH -> "Auto-refreshes every Monday • ⏳ ${grid.daysRemaining} days left"
                                }
                            } else {
                                val diff = com.example.util.BacklogChallengeManager.getWeeksBetween(
                                    com.example.util.BacklogChallengeManager.getCurrentWeekKey(),
                                    grid.weekKey
                                )
                                when {
                                    diff == 1 -> when (lang) {
                                        AppLanguage.SPANISH -> "Vista Previa • Empieza el próximo lunes"
                                        AppLanguage.FRENCH -> "Aperçu à venir • Débute lundi prochain"
                                        AppLanguage.ENGLISH -> "Upcoming Challenge Preview • Starts next Monday"
                                    }
                                    diff == 2 -> when (lang) {
                                        AppLanguage.SPANISH -> "Vista Previa • Empieza en 2 semanas"
                                        AppLanguage.FRENCH -> "Aperçu à venir • Débute dans 2 semaines"
                                        AppLanguage.ENGLISH -> "Upcoming Challenge Preview • Starts in 2 weeks"
                                    }
                                    diff < 0 -> when (lang) {
                                        AppLanguage.SPANISH -> "Archivo Semanal de Retos"
                                        AppLanguage.FRENCH -> "Archives hebdomadaires"
                                        AppLanguage.ENGLISH -> "Weekly Challenge Archive"
                                    }
                                    else -> when (lang) {
                                        AppLanguage.SPANISH -> "Vista Previa del Reto"
                                        AppLanguage.FRENCH -> "Aperçu du défi à venir"
                                        AppLanguage.ENGLISH -> "Upcoming Challenge Preview"
                                    }
                                }
                            },
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { showHowItWorks = !showHowItWorks },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = when (lang) {
                                    AppLanguage.SPANISH -> "Cómo funciona"
                                    AppLanguage.FRENCH -> "Comment ça marche"
                                    AppLanguage.ENGLISH -> "How it works"
                                },
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onRefreshCheck,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = when (lang) {
                                    AppLanguage.SPANISH -> "Comprobación automática"
                                    AppLanguage.FRENCH -> "Vérification auto"
                                    AppLanguage.ENGLISH -> "Auto Check"
                                },
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = s.common.close,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Week Selector Bar (Auto-refresh every Monday + Navigation)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF131D33))
                        .border(1.dp, Color(0xFF1E2E4E), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (weekSelector != null && grid.previousWeekKey.isNotBlank()) {
                        IconButton(
                            onClick = { weekSelector(grid.previousWeekKey) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = s.dialogs.challengePreviousWeek,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(28.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = grid.weekTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        if (grid.isCurrentWeek) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0284C7).copy(alpha = 0.25f))
                                    .border(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.SPANISH -> "ACTIVA"
                                        AppLanguage.FRENCH -> "ACTIVE"
                                        AppLanguage.ENGLISH -> "ACTIVE"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.25f))
                                    .border(0.5.dp, Color(0xFFA78BFA).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .clickable { weekSelector?.invoke(com.example.util.BacklogChallengeManager.getCurrentWeekKey()) }
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.SPANISH -> "VISTA PREVIA (Ir a Activa)"
                                        AppLanguage.FRENCH -> "APERÇU (Aller à l'active)"
                                        AppLanguage.ENGLISH -> "PREVIEW (Tap for Active)"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA78BFA)
                                )
                            }
                        }
                    }

                    if (weekSelector != null && grid.nextWeekKey.isNotBlank()) {
                        IconButton(
                            onClick = { weekSelector(grid.nextWeekKey) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = s.dialogs.challengeNextWeek,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF131D33))
                        .border(1.dp, Color(0xFF1E2E4E), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = s.dialogs.challengeCompletedTilesFormat.format(grid.completedCount),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (grid.isBlackout) Color(0xFFFFD700) else Color(0xFF38BDF8)
                            )
                            Text(
                                text = when {
                                    grid.isBlackout -> when (lang) {
                                        AppLanguage.SPANISH -> "🌟 CARTÓN COMPLETO"
                                        AppLanguage.FRENCH -> "🌟 CARTON PLEIN TERMINÉ"
                                        AppLanguage.ENGLISH -> "🌟 FULL BLACKOUT CLEARED"
                                    }
                                    grid.totalLinesCompleted > 0 -> "⚡ " + s.dialogs.challengeLinesDoneFormat.format(grid.totalLinesCompleted)
                                    else -> when (lang) {
                                        AppLanguage.SPANISH -> "¡Completa una fila, col. o diagonal!"
                                        AppLanguage.FRENCH -> "Complétez une ligne, colonne ou diagonale !"
                                        AppLanguage.ENGLISH -> "Complete any row, col, or diag!"
                                    }
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (grid.totalLinesCompleted > 0) Color(0xFF4ADE80) else TextMuted
                            )
                        }

                        LinearProgressIndicator(
                            progress = { grid.completedCount / 9f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (grid.isBlackout) Color(0xFFFFD700) else Color(0xFF38BDF8),
                            trackColor = Color(0xFF0F172A)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "🎯 Meta: 3 casillas en línea"
                                    AppLanguage.FRENCH -> "🎯 Objectif : aligner 3 cases"
                                    AppLanguage.ENGLISH -> "🎯 Goal: Complete 3-in-a-row"
                                },
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "Desbloqueos manuales: ${grid.manualOverridesUsed}/${grid.maxManualOverrides}"
                                    AppLanguage.FRENCH -> "Validations manuelles : ${grid.manualOverridesUsed}/${grid.maxManualOverrides}"
                                    AppLanguage.ENGLISH -> "Manual Unlocks: ${grid.manualOverridesUsed}/${grid.maxManualOverrides} used"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (grid.manualOverridesUsed >= grid.maxManualOverrides) Color(0xFFFBBF24) else Color(0xFF38BDF8)
                            )
                        }
                    }
                }

                // Expandable How it Works
                AnimatedVisibility(visible = showHowItWorks) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "🎮 Cómo funciona el Bingo de Backlog:"
                                    AppLanguage.FRENCH -> "🎮 Comment fonctionne le Bingo du backlog :"
                                    AppLanguage.ENGLISH -> "🎮 How Backlog Bingo Works:"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH ->
                                        "• Renovación semanal: Cada lunes a las 00:00 tu tablero 3×3 se renueva con nuevos retos de todas las categorías. ¡Tus temas y trofeos desbloqueados permanecen para siempre!\n" +
                                            "• Seguimiento automático: Horas jugadas, juegos terminados y logros de Steam llenan casillas automáticamente.\n" +
                                            "• 10 Hitos y 7 Temas Exclusivos: Gana recompensas al completar 2 casillas, líneas, diagonales, 6 casillas, 2 o 3 líneas y Cartón Lleno.\n" +
                                            "• Desbloqueo manual: ¿Juegas en consola o sin conexión? Puedes marcar manualmente hasta 2 casillas por semana (toca una casilla marcada para liberar el espacio)."
                                    AppLanguage.FRENCH ->
                                        "• Renouvellement hebdomadaire : chaque lundi à 00:00, votre grille 3×3 est actualisée avec de nouveaux défis. Vos thèmes et trophées débloqués restent acquis à vie !\n" +
                                            "• Suivi automatique : le temps de jeu, les jeux terminés et les succès Steam remplissent les cases automatiquement.\n" +
                                            "• 10 étapes & 7 thèmes exclusifs : débloquez trophées et thèmes de couleur avec 2 cases, lignes, colonnes, diagonales, 6 cases, 2/3+ lignes et Carton plein !\n" +
                                            "• Validation manuelle : vous jouez sur console ou hors ligne ? Validez manuellement jusqu'à 2 défis par semaine (touchez une case validée pour la décocher)."
                                    AppLanguage.ENGLISH ->
                                        "• Weekly Auto-Refresh: Every Monday at 00:00, your 3×3 challenge grid automatically refreshes with a fresh set of challenges sampled across all categories. Your unlocked themes and trophies remain permanently unlocked!\n" +
                                            "• Auto-Tracked: Playtime, beaten status, and Steam achievements automatically fill tiles.\n" +
                                            "• 10 Milestones & 7 Reward Themes: Earn trophies and exclusive color themes for 2 tiles, rows, columns, diagonals, 6 tiles, 2+ or 3+ lines, and Full Blackout!\n" +
                                            "• Manual Override: Playing on console/offline? You can manually unlock up to 2 challenges per week (tap an unlocked tile anytime to uncheck and free up a slot)."
                                },
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content: 3x3 Grid + Milestones Shelf
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 3x3 Bingo Grid
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (rowIndex in 0..2) {
                                val isRowComplete = grid.completedRows.contains(rowIndex)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (colIndex in 0..2) {
                                        val tileIndex = rowIndex * 3 + colIndex
                                        val tile = grid.tiles.getOrNull(tileIndex)
                                        if (tile != null) {
                                            val isColComplete = grid.completedColumns.contains(colIndex)
                                            val isDiagComplete = (rowIndex == colIndex && grid.completedDiagonals.contains(0)) ||
                                                    (rowIndex + colIndex == 2 && grid.completedDiagonals.contains(1))
                                            val isInWinningLine = isRowComplete || isColComplete || isDiagComplete || grid.isBlackout

                                            BingoTileCard(
                                                tile = tile,
                                                isInWinningLine = isInWinningLine,
                                                onClick = { selectedTile = tile },
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Milestone Rewards Shelf
                    item {
                        MilestonesShelf(
                            milestones = grid.availableMilestones,
                            onClaimAll = {
                                onClaimMilestones { newlyAwarded ->
                                    val count = newlyAwarded.size
                                    val titles = newlyAwarded.joinToString(", ") { it.localizedTitle(s) }
                                    Toast.makeText(
                                        context,
                                        when (lang) {
                                            AppLanguage.SPANISH -> "🎉 Desbloqueaste $count recompensa(s): $titles"
                                            AppLanguage.FRENCH -> "🎉 $count étape(s) débloquée(s) : $titles !"
                                            AppLanguage.ENGLISH -> "🎉 Unlocked $count Milestone(s): $titles!"
                                        },
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onEquipTheme = onEquipTheme
                        )
                    }
                }
            }
        }
    }

    // Tile Detail Dialog
    selectedTile?.let { tile ->
        TileDetailDialog(
            tile = tile,
            grid = grid,
            onDismiss = { selectedTile = null },
            onToggleManual = {
                val wasManual = tile.manualOverrideDone
                val success = onToggleManualTile(tile.index)
                if (success) {
                    val newManual = !wasManual
                    selectedTile = selectedTile?.copy(
                        isCompleted = if (newManual) true else tile.isCompleted,
                        manualOverrideDone = newManual
                    )
                    if (newManual) {
                        Toast.makeText(
                            context,
                            when (lang) {
                                AppLanguage.SPANISH -> "✅ Casilla marcada manualmente (${grid.manualOverridesUsed + 1}/${grid.maxManualOverrides} usadas)"
                                AppLanguage.FRENCH -> "✅ Défi validé manuellement (${grid.manualOverridesUsed + 1}/${grid.maxManualOverrides} utilisés)"
                                AppLanguage.ENGLISH -> "✅ Challenge manually unlocked (${grid.manualOverridesUsed + 1}/${grid.maxManualOverrides} used)"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            when (lang) {
                                AppLanguage.SPANISH -> "Desbloqueo manual liberado (${(grid.manualOverridesUsed - 1).coerceAtLeast(0)}/${grid.maxManualOverrides} usadas)"
                                AppLanguage.FRENCH -> "Validation manuelle retirée (${(grid.manualOverridesUsed - 1).coerceAtLeast(0)}/${grid.maxManualOverrides} utilisés)"
                                AppLanguage.ENGLISH -> "Manual unlock cleared (${(grid.manualOverridesUsed - 1).coerceAtLeast(0)}/${grid.maxManualOverrides} used)"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        when (lang) {
                            AppLanguage.SPANISH -> "⚠️ ¡Límite alcanzado! Solo puedes marcar manualmente 2 retos por semana."
                            AppLanguage.FRENCH -> "⚠️ Limite atteinte ! Vous ne pouvez valider manuellement que 2 défis par semaine."
                            AppLanguage.ENGLISH -> "⚠️ Manual unlock limit reached! You can only manually unlock 2 challenges per week."
                        },
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }
}

@Composable
private fun BingoTileCard(
    tile: BacklogChallengeTile,
    isInWinningLine: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val lang = s.language
    val lowEnd = LocalLowEndOptimization.current
    val targetBorderColor = when {
        tile.isCompleted && isInWinningLine -> Color(0xFFFFD700)
        tile.isCompleted -> Color(0xFF10B981)
        else -> Color(0xFF1E293B)
    }
    val borderColor = if (lowEnd) {
        targetBorderColor
    } else {
        animateColorAsState(
            targetValue = targetBorderColor,
            label = "tile_border"
        ).value
    }

    val backgroundBrush = if (tile.isCompleted) {
        if (isInWinningLine) {
            Brush.linearGradient(
                listOf(Color(0xFF2E2208), Color(0xFF14241B))
            )
        } else {
            Brush.linearGradient(
                listOf(Color(0xFF0F291E), Color(0xFF0D1B2A))
            )
        }
    } else {
        Brush.linearGradient(
            listOf(Color(0xFF111827), Color(0xFF0F172A))
        )
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundBrush)
            .border(
                width = if (tile.isCompleted && isInWinningLine) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(8.dp)
            .testTag("bingo_tile_${tile.index}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Emoji & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tile.emoji,
                    fontSize = 18.sp
                )

                if (tile.isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isInWinningLine) Color(0xFFFFD700) else Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = when (lang) {
                                AppLanguage.SPANISH -> "Completado"
                                AppLanguage.FRENCH -> "Terminé"
                                AppLanguage.ENGLISH -> "Completed"
                            },
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${tile.currentProgress}/${tile.targetRequirement}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Center: Title
            Text(
                text = tile.localizedTitle(s),
                fontSize = 11.sp,
                fontWeight = if (tile.isCompleted) FontWeight.Bold else FontWeight.SemiBold,
                color = if (tile.isCompleted) {
                    if (isInWinningLine) Color(0xFFFFE082) else Color(0xFF86EFAC)
                } else Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Bottom pill: Category / Manual indicator
            if (tile.manualOverrideDone) {
                Text(
                    text = when (lang) {
                        AppLanguage.SPANISH -> "Manual"
                        AppLanguage.FRENCH -> "Validé"
                        AppLanguage.ENGLISH -> "Manual Done"
                    },
                    fontSize = 8.sp,
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = tile.category.localizedDisplayName(s),
                    fontSize = 8.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
