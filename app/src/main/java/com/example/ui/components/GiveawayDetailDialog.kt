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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import com.example.util.ShareGraphicGenerator
import kotlinx.coroutines.launch
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.GameCategory
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherOption
import com.example.data.model.LauncherType
import com.example.data.model.PlayStatus
import com.example.data.model.SteamDeckStatus
import com.example.data.model.ControllerSupport
import com.example.data.model.SessionStyle
import com.example.data.model.StorageTier
import com.example.ui.density.LocalScreenDensityInfo
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingDarkBackground
import com.example.ui.theme.GamingDarkSurface
import com.example.ui.theme.GamingDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import com.example.ui.theme.TextSecondary

@Composable
fun GiveawayDetailDialog(
    item: GiveawayItem,
    onDismiss: () -> Unit,
    onClaimClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleClaimed: () -> Unit,
    onToggleWishlist: () -> Unit = {},
    onPlayStatusChange: ((PlayStatus) -> Unit)? = null,
    onUpdateManualStorage: ((Float?) -> Unit)? = null,
    onLauncherOptionClick: ((url: String, launcherName: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val screenDensityInfo = LocalScreenDensityInfo.current
    val launcherColor = Color(item.launcherCategory.badgeColorHex)
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary

    val dialogBorderBrush = remember(themePrimary, themeSecondary, launcherColor) {
        Brush.horizontalGradient(
            listOf(
                themePrimary,
                launcherColor.copy(alpha = 0.9f),
                themeSecondary.copy(alpha = 0.7f)
            )
        )
    }

    var dialogVisible by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showShareCardPreview by remember { androidx.compose.runtime.mutableStateOf(false) }
    var isSharingCard by remember { androidx.compose.runtime.mutableStateOf(false) }
    var isStorageDialogOpen by remember { androidx.compose.runtime.mutableStateOf(false) }
    var storageInputText by remember(item.manualInstallSizeGb) {
        androidx.compose.runtime.mutableStateOf(item.manualInstallSizeGb?.toString() ?: "")
    }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

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
                    .border(1.5.dp, dialogBorderBrush, RoundedCornerShape(20.dp))
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
                        // Wishlist toggle button
                        IconButton(
                            onClick = onToggleWishlist,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (item.isWishlisted) Color(0xFFFF2D87).copy(alpha = 0.25f) else Color(0xCC0B0F19))
                                .border(
                                    1.2.dp,
                                    if (item.isWishlisted) Color(0xFFFF2D87) else Color.White.copy(alpha = 0.25f),
                                    CircleShape
                                )
                                .testTag("detail_wishlist_button")
                        ) {
                            Icon(
                                imageVector = if (item.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (item.isWishlisted) "On Wishlist" else "Add to Wishlist",
                                tint = if (item.isWishlisted) Color(0xFFFF2D87) else TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (item.isBookmarked) themePrimary.copy(alpha = 0.25f) else Color(0xCC0B0F19))
                                .border(
                                    1.2.dp,
                                    if (item.isBookmarked) themePrimary else Color.White.copy(alpha = 0.25f),
                                    CircleShape
                                )
                                .testTag("detail_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (item.isBookmarked) "Bookmarked" else "Bookmark",
                                tint = if (item.isBookmarked) themePrimary else TextPrimary,
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

                    // Launcher & Console & Mobile badges on image
                    val isDarkBrand = item.launcherCategory == LauncherType.PLAYSTATION || 
                                      item.launcherCategory == LauncherType.XBOX || 
                                      item.launcherCategory == LauncherType.EPIC ||
                                      item.launcherCategory == LauncherType.IOS
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

                        if (item.isAndroid && item.launcherCategory != LauncherType.ANDROID) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF3DDC84))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Android",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0B0F19)
                                )
                            }
                        }

                        if (item.isIos && item.launcherCategory != LauncherType.IOS) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF007AFF))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "iOS",
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

                    // Value and Type Row with Metascore Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
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
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "100% FREE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0B0F19)
                                )
                            }

                            if (item.isWishlisted) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFF2D87).copy(alpha = 0.25f))
                                        .border(1.dp, Color(0xFFFF2D87), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "❤️ ON WISHLIST",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFF2D87)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GamingDarkSurfaceVariant)
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = item.type,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Metacritic Score Badge (tappable to view Metacritic reviews)
                        MetacriticDetailBadge(
                            info = item.metacriticInfo,
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.metacriticInfo.reviewUrl)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    if (item.categories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item.categories.forEach { cat ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(cat.colorHex).copy(alpha = 0.15f))
                                        .border(0.8.dp, Color(cat.colorHex).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${cat.iconEmoji} ${cat.displayName}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(cat.colorHex)
                                    )
                                }
                            }
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
                            containerColor = themePrimary,
                            contentColor = MaterialTheme.colorScheme.background
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
                                item.isAndroid && !item.isPc -> "Google Play Store"
                                item.isIos && !item.isPc -> "Apple App Store"
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
                            border = androidx.compose.foundation.BorderStroke(1.dp, themePrimary.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Link", fontSize = 12.sp)
                        }

                        // 1-Tap Instant Graphic Card Share
                        Button(
                            onClick = {
                                if (!isSharingCard) {
                                    isSharingCard = true
                                    coroutineScope.launch {
                                        try {
                                            ShareGraphicGenerator.shareGraphicCard(context, item, includeTextLink = true)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isSharingCard = false
                                        }
                                    }
                                }
                            },
                            interactionSource = shareInteraction,
                            modifier = Modifier
                                .weight(1.2f)
                                .graphicsLayer {
                                    scaleX = shareScale
                                    scaleY = shareScale
                                }
                                .testTag("dialog_share_card_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themePrimary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            enabled = !isSharingCard
                        ) {
                            if (isSharingCard) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rendering...", fontSize = 12.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Card", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Preview Card Button
                        OutlinedButton(
                            onClick = { showShareCardPreview = true },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("dialog_preview_card_button"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = themePrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, themePrimary.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Preview Share Card",
                                modifier = Modifier.size(18.dp)
                            )
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

                    // Interactive Game Tracker Section (When Claimed)
                    if (item.isClaimed) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF161F30))
                                .border(1.dp, Color(0xFF2A3D59), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "GAME TRACKER STATUS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeonCyan,
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = item.playStatus.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(item.playStatus.badgeColorHex)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PlayStatus.values().forEach { status ->
                                        val isCurrent = item.playStatus == status
                                        val statusColor = Color(status.badgeColorHex)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isCurrent) statusColor.copy(alpha = 0.2f) else Color(0xFF0F172A))
                                                .border(
                                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                                    color = if (isCurrent) statusColor else Color(0xFF334155),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { onPlayStatusChange?.invoke(status) }
                                                .padding(vertical = 7.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (status) {
                                                    PlayStatus.NOT_STARTED -> "⏳ Backlog"
                                                    PlayStatus.PLAYING -> "🎮 Playing"
                                                    PlayStatus.BEATEN -> "🏆 Finished"
                                                    PlayStatus.DROPPED -> "⏸️ Dropped"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isCurrent) statusColor else TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Steam Telemetry & Achievements Card
                    if (item.hasSteamTelemetry) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF171A21)) // Steam Dark Slate
                                .border(1.dp, Color(0xFF2A475E), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SportsEsports,
                                            contentDescription = "Steam",
                                            tint = Color(0xFF66C0F4),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Steam Activity & Progress",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    if (item.steamAppId != null) {
                                        Text(
                                            text = "App #${item.steamAppId}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF66C0F4),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF1B2838))
                                                .clickable {
                                                    val url = "https://store.steampowered.com/app/${item.steamAppId}"
                                                    try {
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                                    } catch (_: Exception) {}
                                                }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Playtime Logged", fontSize = 10.sp, color = Color(0xFF8F98A0))
                                        Text(
                                            text = "${item.steamPlaytimeHours} (${item.steamPlaytimeMinutes} mins)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    if (item.steamAchievementsTotal > 0) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Achievements", fontSize = 10.sp, color = Color(0xFF8F98A0))
                                            Text(
                                                text = "${item.steamAchievementsUnlocked} / ${item.steamAchievementsTotal} (${item.steamAchievementsPercentage}%)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.steamAchievementsPercentage == 100) NeonGreen else Color.White
                                            )
                                        }
                                    }
                                }

                                if (item.steamAchievementsTotal > 0) {
                                    LinearProgressIndicator(
                                        progress = { (item.steamAchievementsUnlocked.toFloat() / item.steamAchievementsTotal.toFloat()).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (item.steamAchievementsPercentage == 100) NeonGreen else Color(0xFF66C0F4),
                                        trackColor = Color(0xFF1B2838)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val hasValidEndDate = com.example.ui.calendar.ExpirationCalendarUtils.parseExpirationDate(item.endDate) != null
                    if (hasValidEndDate) {
                        OutlinedButton(
                            onClick = {
                                val parsedDate = com.example.ui.calendar.ExpirationCalendarUtils.parseExpirationDate(item.endDate)
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    data = android.provider.CalendarContract.Events.CONTENT_URI
                                    putExtra(android.provider.CalendarContract.Events.TITLE, "Claim Free: ${item.title}")
                                    putExtra(
                                        android.provider.CalendarContract.Events.DESCRIPTION,
                                        "Giveaway for '${item.title}' expires soon!\nPlatforms: ${item.platforms}\nClaim link: ${item.openGiveawayUrl}"
                                    )
                                    putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, item.platforms)
                                    if (parsedDate != null) {
                                        val epochMillis = parsedDate.atTime(18, 0)
                                            .atZone(java.time.ZoneId.systemDefault())
                                            .toInstant()
                                            .toEpochMilli()
                                        putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, epochMillis)
                                        putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, epochMillis + 3600_000L)
                                    }
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "No calendar application installed", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("detail_add_to_calendar_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = themePrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, themePrimary.copy(alpha = 0.45f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = themePrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set Calendar Reminder", fontSize = 13.sp)
                        }
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

                    // Metacritic Critic Consensus & Review Section
                    MetacriticDetailSection(
                        info = item.metacriticInfo,
                        gameTitle = item.title,
                        context = context
                    )

                    Spacer(modifier = Modifier.height(18.dp))

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

                    // Supported Categories Section
                    if (item.categories.isNotEmpty()) {
                        Text(
                            text = "SUPPORTED CATEGORIES & GENRES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F1522))
                                .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.categories.forEach { category ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(category.colorHex).copy(alpha = 0.18f))
                                            .border(1.dp, Color(category.colorHex).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${category.iconEmoji} ${category.displayName}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(category.colorHex)
                                        )
                                    }

                                    Text(
                                        text = category.description,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // HARDWARE & HANDHELD READINESS SECTION
                    val hardware = item.hardwareProfile
                    Text(
                        text = "HARDWARE & HANDHELD READINESS",
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
                            .background(Color(0xFF0F1522))
                            .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Steam Deck Status Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = null,
                                        tint = Color(0xFF66C0F4),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Steam Deck", fontSize = 12.sp, color = TextMuted)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (hardware.deckStatus) {
                                                SteamDeckStatus.VERIFIED -> Color(0x2210B981)
                                                SteamDeckStatus.PLAYABLE -> Color(0x22F59E0B)
                                                SteamDeckStatus.UNSUPPORTED -> Color(0x22EF4444)
                                                SteamDeckStatus.UNKNOWN -> Color(0x2264748B)
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            when (hardware.deckStatus) {
                                                SteamDeckStatus.VERIFIED -> Color(0xFF10B981)
                                                SteamDeckStatus.PLAYABLE -> Color(0xFFF59E0B)
                                                SteamDeckStatus.UNSUPPORTED -> Color(0xFFEF4444)
                                                SteamDeckStatus.UNKNOWN -> Color(0xFF64748B)
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = hardware.deckStatus.pillLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (hardware.deckStatus) {
                                            SteamDeckStatus.VERIFIED -> Color(0xFF10B981)
                                            SteamDeckStatus.PLAYABLE -> Color(0xFFF59E0B)
                                            SteamDeckStatus.UNSUPPORTED -> Color(0xFFEF4444)
                                            SteamDeckStatus.UNKNOWN -> Color(0xFF94A3B8)
                                        }
                                    )
                                }
                            }

                            // Controller Support Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Controller Support", fontSize = 12.sp, color = TextMuted)
                                Text(
                                    text = hardware.controllerSupport.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF66C0F4)
                                )
                            }

                            // Session Style Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Session Style", fontSize = 12.sp, color = TextMuted)
                                Text(
                                    text = hardware.sessionStyle.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (hardware.sessionStyle == SessionStyle.QUICK_SESSION) Color(0xFFF59E0B) else TextPrimary
                                )
                            }

                            // Storage Footprint Tracker Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SdStorage,
                                        contentDescription = null,
                                        tint = Color(0xFFA78BFA),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Install Footprint", fontSize = 12.sp, color = TextMuted)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${hardware.formattedSize} (${hardware.storageTier.label})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA78BFA)
                                        )
                                        Text(
                                            text = if (hardware.isManualOverride) "Manual override" else "Catalog estimate",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            storageInputText = item.manualInstallSizeGb?.toString() ?: ""
                                            isStorageDialogOpen = true
                                        },
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("detail_edit_storage_btn"),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit size",
                                            tint = Color(0xFFA78BFA),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit", fontSize = 11.sp, color = Color(0xFFA78BFA))
                                    }
                                }
                            }
                        }
                    }

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
                            if (item.categories.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Categories", fontSize = 12.sp, color = TextMuted)
                                    Text(
                                        text = item.categories.joinToString(", ") { it.displayName },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 210.dp)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    MetacriticScorePill(score = item.metacriticScore)
                                    Text("Metacritic Score", fontSize = 12.sp, color = TextMuted)
                                }
                                Text(
                                    text = "${item.metacriticScore}/100 • ${item.metacriticInfo.tier.label}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = item.metacriticInfo.tier.badgeColor
                                )
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

    if (showShareCardPreview) {
        ShareCardPreviewDialog(
            item = item,
            onDismiss = { showShareCardPreview = false }
        )
    }

    if (isStorageDialogOpen) {
        Dialog(
            onDismissRequest = { isStorageDialogOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SdStorage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Storage Footprint Tracker",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.title,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = "Track the exact install size on your Steam Deck, ROG Ally, or PC drive. Enter manual gigabytes (GB) or reset to restore estimated size (${item.hardwareProfile.estimatedSizeGb} GB).",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    OutlinedTextField(
                        value = storageInputText,
                        onValueChange = { storageInputText = it },
                        label = { Text("Install Size (GB)") },
                        placeholder = { Text("e.g. 15.5") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.manualInstallSizeGb != null) {
                            TextButton(
                                onClick = {
                                    onUpdateManualStorage?.invoke(null)
                                    isStorageDialogOpen = false
                                }
                            ) {
                                Text("Reset to Auto", color = Color(0xFFEF4444))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        TextButton(onClick = { isStorageDialogOpen = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val parsed = storageInputText.trim().toFloatOrNull()
                                onUpdateManualStorage?.invoke(parsed)
                                isStorageDialogOpen = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save Size")
                        }
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
