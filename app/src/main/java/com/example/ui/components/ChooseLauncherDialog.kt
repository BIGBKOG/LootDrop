package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherOption
import com.example.data.model.LauncherType
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingDarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocalLowEndOptimization

@Composable
fun ChooseLauncherDialog(
    item: GiveawayItem,
    ownershipStatus: com.example.ui.GiveawaysViewModel.OwnershipStatus? = null,
    onDismiss: () -> Unit,
    onLauncherClaimed: (url: String, launcherName: String) -> Unit,
    launchers: List<LauncherOption> = item.getAvailableLaunchers(LocalAppStrings.current.language)
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val isFr = s.language == AppLanguage.FRENCH
    val lowEnd = LocalLowEndOptimization.current
    val dialogContainerModifier = if (lowEnd) {
        Modifier
            .fillMaxWidth(0.94f)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, GamingCardBorder, RoundedCornerShape(20.dp))
            .testTag("choose_launcher_dialog")
    } else {
        var dialogVisible by remember { androidx.compose.runtime.mutableStateOf(false) }
        androidx.compose.runtime.LaunchedEffect(Unit) {
            dialogVisible = true
        }
        val dialogScale by animateFloatAsState(
            targetValue = if (dialogVisible) 1f else 0.88f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "choose_launcher_dialog_scale"
        )
        val dialogAlpha by animateFloatAsState(
            targetValue = if (dialogVisible) 1f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "choose_launcher_dialog_alpha"
        )
        Modifier
            .fillMaxWidth(0.94f)
            .graphicsLayer {
                scaleX = dialogScale
                scaleY = dialogScale
                alpha = dialogAlpha
            }
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, GamingCardBorder, RoundedCornerShape(20.dp))
            .testTag("choose_launcher_dialog")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = dialogContainerModifier,
            color = GamingDarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = s.dialogs.chooseLauncherTitle,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            val platformCount = launchers.filter { it.launcherType != LauncherType.OTHER }.size
                            Text(
                                text = when {
                                    isEs -> "Disponible en $platformCount plataformas"
                                    isFr -> "Disponible sur $platformCount plateformes"
                                    else -> "Available on $platformCount platforms"
                                },
                                fontSize = 12.sp,
                                color = NeonGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_choose_launcher_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = s.common.close,
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Game Mini Card Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F1522))
                        .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        val thumbUrl = if (item.thumbnail.isNotBlank()) item.thumbnail else item.image
                        val thumbRequest = remember(context, thumbUrl, lowEnd) {
                            buildGiveawayImageRequest(
                                context = context,
                                url = thumbUrl,
                                isLowEndOptimization = lowEnd,
                                width = 128,
                                height = 128
                            )
                        }
                        AsyncImage(
                            model = thumbRequest,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = when {
                                    isEs -> "100% GRATIS"
                                    isFr -> "100 % GRATUIT"
                                    else -> "100% OFF"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonGreen
                            )
                            if (item.worth.isNotBlank() && item.worth != "N/A" && item.worth != "FREE") {
                                Text(
                                    text = item.worth,
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            }
                        }
                    }
                }

                // Prominent Ownership Banner Pill: Check Vault and Steam Library
                if (ownershipStatus?.isOwned == true) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .border(1.2.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                            .testTag("choose_launcher_ownership_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (ownershipStatus.inSteam) "🎮" else "💎",
                                fontSize = 16.sp
                            )
                            Column {
                                Text(
                                    text = when {
                                        isEs -> "YA TIENES ESTE JUEGO"
                                        isFr -> "VOUS POSSÉDEZ DÉJÀ CE JEU"
                                        else -> "YOU ALREADY HAVE THIS"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF10B981),
                                    letterSpacing = 0.5.sp
                                )
                                val sourceText = when {
                                    isEs -> when {
                                        ownershipStatus.inVault && ownershipStatus.inSteam -> "En tu Bóveda y biblioteca de Steam"
                                        ownershipStatus.inVault -> "Ya guardado en tu Bóveda"
                                        else -> "Ya está en tu biblioteca de Steam"
                                    }
                                    isFr -> when {
                                        ownershipStatus.inVault && ownershipStatus.inSteam -> "Dans votre Coffre et bibliothèque Steam"
                                        ownershipStatus.inVault -> "Déjà enregistré dans votre Coffre"
                                        else -> "Déjà dans votre bibliothèque Steam"
                                    }
                                    else -> when {
                                        ownershipStatus.inVault && ownershipStatus.inSteam -> "In your Vault & Steam Library"
                                        ownershipStatus.inVault -> "Already saved in your Vault"
                                        else -> "Already in your Steam Library"
                                    }
                                }
                                Text(
                                    text = sourceText,
                                    fontSize = 10.sp,
                                    color = Color(0xFFD1FAE5),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = when {
                        isEs -> "Selecciona en qué tienda reclamar este juego:"
                        isFr -> "Choisissez sur quelle boutique réclamer ce jeu :"
                        else -> "Select which launcher to claim this game on:"
                    },
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // List of Launchers
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    launchers.forEach { option ->
                        LauncherOptionRow(
                            option = option,
                            onClick = {
                                onLauncherClaimed(option.url, option.name)
                                onDismiss()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_launcher_chooser_button")
                ) {
                    Text(
                        text = s.common.cancel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun LauncherOptionRow(
    option: LauncherOption,
    onClick: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val isFr = s.language == AppLanguage.FRENCH
    val lowEnd = LocalLowEndOptimization.current
    val interactionSource = remember { MutableInteractionSource() }
    val brandColor = Color(option.badgeColorHex)
    val rowModifier = if (lowEnd) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, brandColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
    } else {
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.965f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "launcher_row_scale"
        )
        Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, brandColor.copy(alpha = if (isPressed) 0.9f else 0.45f), RoundedCornerShape(12.dp))
    }

    Card(
        modifier = rowModifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("launcher_option_${option.name.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(brandColor)
                )

                Column {
                    Text(
                        text = option.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = option.subtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(brandColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            isEs -> "Reclamar"
                            isFr -> "Réclamer"
                            else -> "Claim"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandColor
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = s.common.open,
                    tint = brandColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
