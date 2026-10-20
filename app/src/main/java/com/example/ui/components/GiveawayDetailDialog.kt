package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherOption
import com.example.data.model.LauncherType
import com.example.ui.density.LocalScreenDensityInfo
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingDarkBackground
import com.example.ui.theme.GamingDarkSurface
import com.example.ui.theme.GamingDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GiveawayDetailDialog(
    item: GiveawayItem,
    onDismiss: () -> Unit,
    onClaimClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleClaimed: () -> Unit,
    onLauncherOptionClick: ((url: String, launcherName: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val screenDensityInfo = LocalScreenDensityInfo.current
    val launcherColor = Color(item.launcherCategory.badgeColorHex)

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
        label = "detail_dialog_scale"
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = if (dialogVisible) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "detail_dialog_alpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(screenDensityInfo.adaptiveHorizontalPadding),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = screenDensityInfo.maxDialogWidth)
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = dialogScale
                        scaleY = dialogScale
                        alpha = dialogAlpha
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(20.dp))
                    .testTag("giveaway_detail_dialog"),
                color = GamingDarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Image Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(minOf(240.dp, (screenDensityInfo.cardCoverHeight.value * 1.25f).dp))
                    ) {
                    val detailImageRequest = remember(item.id, item.image, item.thumbnail) {
                        ImageRequest.Builder(context)
                            .data(if (item.image.isNotBlank()) item.image else item.thumbnail)
                            .crossfade(true)
                            .size(960, 540)
                            .build()
                    }
                    AsyncImage(
                        model = detailImageRequest,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x800B0F19),
                                        Color.Transparent,
                                        GamingDarkSurface
                                    )
                                )
                            )
                    )

                    // Header Controls: Bookmark & Close Buttons with fluid spring interaction
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val bookmarkScale by animateFloatAsState(
                            targetValue = if (item.isBookmarked) 1.22f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "detail_bookmark_scale"
                        )
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (item.isBookmarked) NeonCyan.copy(alpha = 0.25f) else Color(0xCC0B0F19))
                                .border(
                                    1.dp,
                                    if (item.isBookmarked) NeonCyan else Color.White.copy(alpha = 0.25f),
                                    CircleShape
                                )
                                .testTag("detail_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (item.isBookmarked) "Bookmarked" else "Bookmark",
                                tint = if (item.isBookmarked) NeonCyan else TextPrimary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer {
                                        scaleX = bookmarkScale
                                        scaleY = bookmarkScale
                                    }
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xCC0B0F19))
                                .testTag("detail_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextPrimary
                            )
                        }
                    }

                    // Launcher & Console badges on image
                    val isDarkBrand = item.launcherCategory == LauncherType.PLAYSTATION || 
                                      item.launcherCategory == LauncherType.XBOX || 
                                      item.launcherCategory == LauncherType.EPIC
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(launcherColor)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = item.launcherCategory.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkBrand) Color.White else Color(0xFF0B0F19)
                            )
                        }

                        if (item.isPlayStation && item.launcherCategory != LauncherType.PLAYSTATION) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0070D1))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "PlayStation",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (item.isXbox && item.launcherCategory != LauncherType.XBOX) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF107C10))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Xbox",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Content Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    // Title
                    Text(
                        text = item.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Value and Type Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.worth.isNotBlank() && item.worth != "N/A" && item.worth != "FREE") {
                            Text(
                                text = item.worth,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonGreen)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "100% FREE TO KEEP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0B0F19)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GamingDarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = item.type,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Claim CTA Button with fluid spring scale
                    val detailClaimInteractionSource = remember { MutableInteractionSource() }
                    val isDetailClaimPressed by detailClaimInteractionSource.collectIsPressedAsState()
                    val detailClaimScale by animateFloatAsState(
                        targetValue = if (isDetailClaimPressed) 0.96f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "detail_claim_scale"
                    )

                    Button(
                        onClick = onClaimClick,
                        interactionSource = detailClaimInteractionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .graphicsLayer {
                                scaleX = detailClaimScale
                                scaleY = detailClaimScale
                            }
                            .testTag("detail_claim_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color(0xFF0B0F19)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            val claimDestination = when {
                                item.hasMultipleLaunchers -> "Choose Store / Launcher ▾"
                                item.isPlayStation && !item.isPc -> "PlayStation Store"
                                item.isXbox && !item.isPc -> "Xbox Store"
                                else -> item.launcherCategory.displayName
                            }
                            Text(
                                text = if (item.hasMultipleLaunchers) "Claim • Choose Launcher ▾" else "Claim on $claimDestination",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Direct Launcher Options when game is 100% off across multiple stores/launchers
                    if (item.hasMultipleLaunchers) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                            )
                            Text(
                                text = "AVAILABLE LAUNCHERS (100% OFF)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonGreen,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.availableLaunchers.forEach { launcherOption ->
                                DetailLauncherCardRow(
                                    launcherOption = launcherOption,
                                    onClick = {
                                        if (onLauncherOptionClick != null) {
                                            onLauncherOptionClick(launcherOption.url, launcherOption.name)
                                        } else {
                                            onClaimClick()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Action Buttons: Copy Link, Share, Bookmark, Claimed
                    val copyInteraction = remember { MutableInteractionSource() }
                    val isCopyPressed by copyInteraction.collectIsPressedAsState()
                    val copyScale by animateFloatAsState(
                        targetValue = if (isCopyPressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "copy_scale"
                    )

                    val shareInteraction = remember { MutableInteractionSource() }
                    val isSharePressed by shareInteraction.collectIsPressedAsState()
                    val shareScale by animateFloatAsState(
                        targetValue = if (isSharePressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "share_scale"
                    )

                    val libraryInteraction = remember { MutableInteractionSource() }
                    val isLibraryPressed by libraryInteraction.collectIsPressedAsState()
                    val libraryScale by animateFloatAsState(
                        targetValue = if (isLibraryPressed) 0.96f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "library_scale"
                    )

                    val libraryCheckScale by animateFloatAsState(
                        targetValue = if (item.isClaimed) 1.25f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                        label = "library_check_scale"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Giveaway Link", item.openGiveawayUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Claim link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            interactionSource = copyInteraction,
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer {
                                    scaleX = copyScale
                                    scaleY = copyScale
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GamingCardBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Link", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Free Game: ${item.title}")
                                    putExtra(Intent.EXTRA_TEXT, "Grab '${item.title}' 100% free on ${item.platforms}!\n${item.openGiveawayUrl}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share free game"))
                            },
                            interactionSource = shareInteraction,
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer {
                                    scaleX = shareScale
                                    scaleY = shareScale
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GamingCardBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onToggleClaimed,
                        interactionSource = libraryInteraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = libraryScale
                                scaleY = libraryScale
                            }
                            .testTag("dialog_toggle_library_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (item.isClaimed) NeonGreen.copy(alpha = 0.15f) else Color.Transparent,
                            contentColor = if (item.isClaimed) NeonGreen else TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (item.isClaimed) NeonGreen else GamingCardBorder)
                    ) {
                        Icon(
                            imageVector = if (item.isClaimed) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .graphicsLayer {
                                    scaleX = libraryCheckScale
                                    scaleY = libraryCheckScale
                                }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (item.isClaimed) "In Library (Claimed)" else "Add to 'In Library'", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Redemption Instructions section
                    if (item.instructions.isNotBlank()) {
                        Text(
                            text = "HOW TO CLAIM",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(GamingDarkSurfaceVariant)
                                .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = item.instructions.replace("\r", ""),
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 19.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // About Description
                    Text(
                        text = "ABOUT THIS GIVEAWAY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.description,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Details metadata box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F1522))
                            .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Platforms", fontSize = 12.sp, color = TextMuted)
                                Text(item.platforms, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Expiration Date", fontSize = 12.sp, color = TextMuted)
                                Text(item.endDate, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NeonGreen)
                            }
                            if (item.users > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Claimed by", fontSize = 12.sp, color = TextMuted)
                                    Text("${item.users} gamers", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
}

@Composable
private fun DetailLauncherCardRow(
    launcherOption: LauncherOption,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "detail_launcher_scale"
    )
    val optColor = Color(launcherOption.badgeColorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, optColor.copy(alpha = if (isPressed) 0.85f else 0.5f), RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("detail_launcher_${launcherOption.name.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.GamingDarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(optColor)
                )
                Column {
                    Text(
                        text = launcherOption.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.TextPrimary
                    )
                    Text(
                        text = launcherOption.subtitle,
                        fontSize = 11.sp,
                        color = com.example.ui.theme.TextSecondary
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(optColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Claim",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = optColor
                    )
                }
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = optColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
