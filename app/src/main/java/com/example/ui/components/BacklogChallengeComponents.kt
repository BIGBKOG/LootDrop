package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BacklogChallengeGrid
import com.example.data.model.BacklogChallengeTile
import com.example.data.model.BacklogMilestoneReward
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedDescription
import com.example.i18n.localizedDisplayName
import com.example.i18n.localizedSubtitle
import com.example.i18n.localizedTitle
import com.example.preferences.AccentTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
internal fun MilestonesShelf(
    milestones: List<BacklogMilestoneReward>,
    onClaimAll: () -> Unit,
    onEquipTheme: (AccentTheme) -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val unclaimedCount = milestones.count { it.isUnlocked && !it.isClaimed }
    val claimedCount = milestones.count { it.isClaimed }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111827))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isEs) "🏆 Recompensas de Hitos" else "🏆 Milestone Rewards",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(0.5.dp, Color(0xFF334155), CircleShape)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$claimedCount / ${milestones.size}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (claimedCount > 0) Color(0xFF34D399) else Color(0xFF94A3B8)
                    )
                }
                if (unclaimedCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isEs) "$unclaimedCount NUEVAS" else "$unclaimedCount NEW",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            if (unclaimedCount > 0) {
                Button(
                    onClick = onClaimAll,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("claim_all_backlog_milestones")
                ) {
                    Text(
                        text = if (isEs) "Reclamar Todo" else "Claim All",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        milestones.forEach { milestone ->
            MilestoneItemRow(
                milestone = milestone,
                onEquipTheme = onEquipTheme
            )
        }
    }
}

@Composable
internal fun MilestoneItemRow(
    milestone: BacklogMilestoneReward,
    onEquipTheme: (AccentTheme) -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val isUnlocked = milestone.isUnlocked
    val isClaimed = milestone.isClaimed
    val rewardTheme = milestone.themeReward

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isClaimed) Color(0xFF14241B) else if (isUnlocked) Color(0xFF1E293B) else Color(0xFF0F172A))
            .border(
                1.dp,
                if (isClaimed) Color(0xFF10B981).copy(alpha = 0.5f) else if (isUnlocked) Color(0xFFF59E0B).copy(alpha = 0.6f) else Color(0xFF1E293B),
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isClaimed -> Color(0xFF10B981).copy(alpha = 0.2f)
                        isUnlocked -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                        rewardTheme != null -> rewardTheme.darkPrimary.copy(alpha = 0.14f)
                        else -> Color(0xFF1E293B)
                    }
                )
                .then(
                    if (rewardTheme != null) {
                        Modifier.border(1.dp, rewardTheme.darkPrimary.copy(alpha = 0.45f), CircleShape)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isClaimed -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = if (isEs) "Reclamado" else "Claimed",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
                isUnlocked -> Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = if (isEs) "Desbloqueado" else "Unlocked",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
                else -> Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = if (isEs) "Bloqueado" else "Locked",
                    tint = rewardTheme?.darkPrimary?.copy(alpha = 0.8f) ?: Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = milestone.localizedTitle(s),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isClaimed) Color(0xFF86EFAC) else if (isUnlocked) Color(0xFFFDE68A) else Color(0xFF94A3B8),
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (rewardTheme != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(rewardTheme.darkPrimary)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(rewardTheme.darkSecondary)
                        )
                    }
                }
            }
            Text(
                text = milestone.localizedSubtitle(s),
                fontSize = 10.sp,
                color = TextSecondary
            )
        }

        if (isClaimed && rewardTheme != null) {
            Button(
                onClick = { onEquipTheme(rewardTheme) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(28.dp)
                    .testTag("equip_theme_${rewardTheme.name.lowercase()}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                    Text(if (isEs) "Equipar" else "Equip", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else if (isClaimed) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isEs) "En Vitrina" else "In Showcase",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399)
                )
            }
        } else {
            Text(
                text = if (isUnlocked) (if (isEs) "Listo para Reclamar" else "Ready to Claim") else (if (isEs) "Bloqueado" else "Locked"),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isUnlocked) Color(0xFFFBBF24) else TextMuted
            )
        }
    }
}

@Composable
internal fun TileDetailDialog(
    tile: BacklogChallengeTile,
    grid: BacklogChallengeGrid,
    onDismiss: () -> Unit,
    onToggleManual: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = tile.emoji, fontSize = 28.sp)
                        Column {
                            Text(
                                text = tile.localizedTitle(s),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isEs) {
                                    "Categoría: ${tile.category.localizedDisplayName(s)}"
                                } else {
                                    "Category: ${tile.category.localizedDisplayName(s)}"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = s.common.close,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp)
                ) {
                    Text(
                        text = tile.localizedDescription(s),
                        fontSize = 13.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 18.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEs) {
                            "Progreso: ${tile.currentProgress} / ${tile.targetRequirement}"
                        } else {
                            "Progress: ${tile.currentProgress} / ${tile.targetRequirement}"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tile.isCompleted) Color(0xFF34D399) else Color(0xFF38BDF8)
                    )

                    if (tile.isCompleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (tile.manualOverrideDone) {
                                    if (isEs) "Manual" else "Manual Done"
                                } else {
                                    if (isEs) "Auto-Completado" else "Auto-Completed"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF34D399)
                            )
                        }
                    }
                }

                tile.recommendedGameTitle?.let { title ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEs) "💡 Sugerido de tu Biblioteca:" else "💡 Recommended From Library:",
                                    fontSize = 10.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (tile.recommendedAppId != null && tile.recommendedAppId > 0) {
                                TextButton(
                                    onClick = {
                                        try {
                                            val steamUri = Uri.parse("steam://run/${tile.recommendedAppId}")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, steamUri))
                                        } catch (_: Exception) {
                                            val webUri = Uri.parse("https://store.steampowered.com/app/${tile.recommendedAppId}")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                        }
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(if (isEs) "Abrir" else "Launch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = onToggleManual,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_manual_tile_${tile.index}"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = when {
                                tile.manualOverrideDone -> Color(0xFFEF4444)
                                grid.canManualOverrideMore -> Color(0xFFF59E0B)
                                else -> Color(0xFF94A3B8)
                            }
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = when {
                                tile.manualOverrideDone -> if (isEs) "Quitar Marca Manual (Liberar espacio)" else "Clear Manual Unlock (Free up slot)"
                                grid.canManualOverrideMore -> if (isEs) "Marcar como Hecho (${grid.remainingManualOverrides} disp.)" else "Mark Done Manually (${grid.remainingManualOverrides} left)"
                                else -> if (isEs) "Límite Manual Alcanzado (2/2 Usados)" else "Manual Unlock Limit Reached (2/2 Used)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = when {
                            tile.manualOverrideDone -> if (isEs) "Toca para desmarcar y liberar 1 de tus 2 usos semanales" else "Tap to uncheck and free up 1 of your 2 weekly manual unlocks"
                            grid.canManualOverrideMore -> if (isEs) {
                                "Margen: ${grid.manualOverridesUsed}/${grid.maxManualOverrides} usados esta semana"
                            } else {
                                "Allowance: ${grid.manualOverridesUsed}/${grid.maxManualOverrides} used this week"
                            }
                            else -> if (isEs) {
                                "Límite de 2 desbloqueos manuales esta semana. Libera otra casilla primero."
                            } else {
                                "Limit of 2 manual unlocks reached this week. Clear another tile first to use here."
                            }
                        },
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
