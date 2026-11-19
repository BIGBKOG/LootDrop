package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GiveawayItem
import com.example.data.model.PlayStatus
import com.example.data.model.PublicReview
import com.example.data.repository.OfficialOstRepository
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedDescription
import com.example.i18n.localizedDisplayName
import com.example.ui.GiveawaysViewModel
import com.example.ui.calendar.ExpirationCalendarUtils
import com.example.ui.density.LocalScreenDensityInfo
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingDarkSurface
import com.example.ui.theme.GamingDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareGraphicGenerator
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.Locale

@Composable
fun GiveawayDetailDialog(
    item: GiveawayItem,
    ownershipStatus: GiveawaysViewModel.OwnershipStatus? = null,
    reviews: List<PublicReview> = emptyList(),
    defaultAuthorName: String = "Gamer",
    onDismiss: () -> Unit,
    onClaimClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleClaimed: () -> Unit,
    onToggleWishlist: () -> Unit = {},
    onPlayStatusChange: ((PlayStatus) -> Unit)? = null,
    onUpdateManualStorage: ((Float?) -> Unit)? = null,
    onLauncherOptionClick: ((url: String, launcherName: String) -> Unit)? = null,
    onPostReview: ((rating: Int, title: String, comment: String) -> Unit)? = null,
    onVoteHelpful: ((reviewId: String) -> Unit)? = null,
    hasVotedHelpful: ((reviewId: String) -> Boolean)? = null
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val lang = s.language
    val screenDensityInfo = LocalScreenDensityInfo.current
    val launcherColor = Color(item.launcherCategory.badgeColorHex)
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary

    val dialogBorderBrush = remember(themePrimary, themeSecondary, launcherColor) {
        Brush.horizontalGradient(listOf(themePrimary, launcherColor.copy(alpha = 0.9f), themeSecondary.copy(alpha = 0.7f)))
    }

    var dialogVisible by remember { mutableStateOf(false) }
    var showShareCardPreview by remember { mutableStateOf(false) }
    var isSharingCard by remember { mutableStateOf(false) }
    var isStorageDialogOpen by remember { mutableStateOf(false) }
    var storageInputText by remember(item.manualInstallSizeGb) { mutableStateOf(item.manualInstallSizeGb?.toString() ?: "") }
    val coroutineScope = rememberCoroutineScope()
    val ostRepo = remember(context) { OfficialOstRepository.getInstance(context) }
    var ostInfo by remember(item.id, item.title, item.type) { mutableStateOf(ostRepo.getInstantOstInfo(item)) }
    var isOstLoading by remember(item.id, item.title, item.type) { mutableStateOf(ostInfo == null && ostRepo.isEligibleForOst(item)) }

    LaunchedEffect(item.id, item.title) {
        dialogVisible = true
        if (ostRepo.isEligibleForOst(item)) {
            ostInfo = ostRepo.fetchOstInfo(item)
        } else ostInfo = null
        isOstLoading = false
    }

    val dialogScale by animateFloatAsState(if (dialogVisible) 1f else 0.88f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), label = "detail_dialog_scale")
    val dialogAlpha by animateFloatAsState(if (dialogVisible) 1f else 0f, spring(stiffness = Spring.StiffnessMediumLow), label = "detail_dialog_alpha")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.fillMaxSize().padding(screenDensityInfo.adaptiveHorizontalPadding),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = screenDensityInfo.maxDialogWidth)
                    .fillMaxWidth()
                    .graphicsLayer { scaleX = dialogScale; scaleY = dialogScale; alpha = dialogAlpha }
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, dialogBorderBrush, RoundedCornerShape(20.dp))
                    .testTag("giveaway_detail_dialog"),
                color = GamingDarkSurface
            ) {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    GiveawayDetailHeroBanner(
                        item = item,
                        context = context,
                        coverHeight = minOf(240.dp, (screenDensityInfo.cardCoverHeight.value * 1.25f).dp),
                        launcherColor = launcherColor,
                        themePrimary = themePrimary,
                        onToggleWishlist = onToggleWishlist,
                        onToggleBookmark = onToggleBookmark,
                        onDismiss = onDismiss
                    )

                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp)) {
                        Text(item.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f, fill = false)) {
                                if (item.worth.isNotBlank() && item.worth != "N/A" && item.worth != "FREE") {
                                    Text(item.worth, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMuted, textDecoration = TextDecoration.LineThrough)
                                }
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(NeonGreen).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Text(s.home.badgeFree, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0B0F19))
                                }
                                if (item.isWishlisted) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFF2D87).copy(alpha = 0.25f))
                                            .border(1.dp, Color(0xFFFF2D87), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 7.dp, vertical = 4.dp)
                                    ) {
                                        Text("❤️ ${s.home.badgeWishlisted}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF2D87))
                                    }
                                }
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(GamingDarkSurfaceVariant).padding(horizontal = 7.dp, vertical = 4.dp)) {
                                    val localizedType = when (item.type.lowercase()) {
                                        "game" -> when (lang) {
                                            AppLanguage.SPANISH -> "Juego"
                                            AppLanguage.FRENCH -> "Jeu"
                                            AppLanguage.ENGLISH -> item.type
                                        }
                                        "dlc" -> "DLC"
                                        "early access" -> when (lang) {
                                            AppLanguage.SPANISH -> "Acceso Anticipado"
                                            AppLanguage.FRENCH -> "Accès anticipé"
                                            AppLanguage.ENGLISH -> item.type
                                        }
                                        else -> item.type
                                    }
                                    Text(localizedType, fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            MetacriticDetailBadge(info = item.metacriticInfo) {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.metacriticInfo.reviewUrl)).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                                } catch (_: Exception) {
                                    Toast.makeText(
                                        context,
                                        when (lang) {
                                            AppLanguage.SPANISH -> "No se pudo abrir el navegador"
                                            AppLanguage.FRENCH -> "Impossible d'ouvrir le navigateur"
                                            AppLanguage.ENGLISH -> "Could not open browser"
                                        },
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        var isDirectWriteDialogOpen by remember { mutableStateOf(false) }
                        val avgGameRating = remember(reviews) { if (reviews.isEmpty()) 5.0 else reviews.map { it.rating }.average() }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E1B13))
                                .border(1.dp, Color(0xFFFFB800).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable { isDirectWriteDialogOpen = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("detail_reviews_quick_pill"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("⭐ ${String.format(Locale.US, "%.1f", avgGameRating)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFB800))
                                val reviewWord = when (lang) {
                                    AppLanguage.SPANISH -> if (reviews.size == 1) "reseña" else "reseñas"
                                    AppLanguage.FRENCH -> if (reviews.size == 1) "avis" else "avis"
                                    AppLanguage.ENGLISH -> if (reviews.size == 1) "Player Review" else "Player Reviews"
                                }
                                Text("• ${reviews.size} $reviewWord", fontSize = 12.sp, color = TextPrimary)
                            }
                            Text("${s.dialogs.reviewsWriteReview} ✍️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB800))
                        }

                        if (isDirectWriteDialogOpen) {
                            WriteReviewDialog(
                                gameTitle = item.title,
                                defaultAuthorName = defaultAuthorName,
                                initialIsAppReview = false,
                                onDismiss = { isDirectWriteDialogOpen = false },
                                onSubmit = { rating, title, comment, _ ->
                                    onPostReview?.invoke(rating, title, comment)
                                    isDirectWriteDialogOpen = false
                                }
                            )
                        }

                        if (item.categories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                item.categories.forEach { cat ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(cat.colorHex).copy(alpha = 0.15f))
                                            .border(0.8.dp, Color(cat.colorHex).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Text("${cat.iconEmoji} ${cat.localizedDisplayName(s)}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(cat.colorHex))
                                    }
                                }
                            }
                        }

                        if (ownershipStatus?.isOwned == true) {
                            Spacer(modifier = Modifier.height(14.dp))
                            DetailOwnershipBanner(ownershipStatus = ownershipStatus)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        DetailActionButtonsBlock(
                            item = item, context = context, themePrimary = themePrimary, isSharingCard = isSharingCard,
                            onClaimClick = onClaimClick, onLauncherOptionClick = onLauncherOptionClick,
                            onShareCardClick = {
                                if (!isSharingCard) {
                                    isSharingCard = true
                                    coroutineScope.launch {
                                        try { ShareGraphicGenerator.shareGraphicCard(context, item, includeTextLink = true) }
                                        catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                when (lang) {
                                                    AppLanguage.SPANISH -> "Error al compartir: ${e.message}"
                                                    AppLanguage.FRENCH -> "Erreur de partage : ${e.message}"
                                                    AppLanguage.ENGLISH -> "Share error: ${e.message}"
                                                },
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        finally { isSharingCard = false }
                                    }
                                }
                            },
                            onPreviewCardClick = { showShareCardPreview = true },
                            onToggleClaimed = onToggleClaimed
                        )

                        if (item.isClaimed) {
                            Spacer(modifier = Modifier.height(10.dp))
                            DetailGameTrackerCard(item = item, onPlayStatusChange = onPlayStatusChange)
                        }
                        if (item.hasSteamTelemetry) {
                            Spacer(modifier = Modifier.height(10.dp))
                            DetailSteamTelemetryCard(item = item, context = context)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        val parsedEndDate = ExpirationCalendarUtils.parseExpirationDate(item.endDate)
                        if (parsedEndDate != null) {
                            OutlinedButton(
                                onClick = {
                                    val epochMillis = parsedEndDate.atTime(18, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    val intent = Intent(Intent.ACTION_INSERT).apply {
                                        data = CalendarContract.Events.CONTENT_URI
                                        putExtra(
                                            CalendarContract.Events.TITLE,
                                            when (lang) {
                                                AppLanguage.SPANISH -> "Reclamar Gratis: ${item.title}"
                                                AppLanguage.FRENCH -> "Réclamer gratuitement : ${item.title}"
                                                AppLanguage.ENGLISH -> "Claim Free: ${item.title}"
                                            }
                                        )
                                        putExtra(
                                            CalendarContract.Events.DESCRIPTION,
                                            when (lang) {
                                                AppLanguage.SPANISH -> "¡La oferta de '${item.title}' expira pronto!\nPlataformas: ${item.platforms}\nEnlace: ${item.openGiveawayUrl}"
                                                AppLanguage.FRENCH -> "L'offre pour « ${item.title} » expire bientôt !\nPlateformes : ${item.platforms}\nLien : ${item.openGiveawayUrl}"
                                                AppLanguage.ENGLISH -> "Giveaway for '${item.title}' expires soon!\nPlatforms: ${item.platforms}\nClaim link: ${item.openGiveawayUrl}"
                                            }
                                        )
                                        putExtra(CalendarContract.Events.EVENT_LOCATION, item.platforms)
                                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, epochMillis)
                                        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, epochMillis + 3600_000L)
                                    }
                                    try { context.startActivity(intent) }
                                    catch (_: Exception) {
                                        Toast.makeText(
                                            context,
                                            when (lang) {
                                                AppLanguage.SPANISH -> "No hay aplicación de calendario instalada"
                                                AppLanguage.FRENCH -> "Aucune application d'agenda installée"
                                                AppLanguage.ENGLISH -> "No calendar application installed"
                                            },
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("detail_add_to_calendar_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = themePrimary),
                                border = BorderStroke(1.dp, themePrimary.copy(alpha = 0.45f))
                            ) {
                                Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp), tint = themePrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    when (lang) {
                                        AppLanguage.SPANISH -> "Añadir recordatorio al calendario"
                                        AppLanguage.FRENCH -> "Ajouter un rappel au calendrier"
                                        AppLanguage.ENGLISH -> "Set Calendar Reminder"
                                    },
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        if (item.instructions.isNotBlank()) {
                            Text(s.dialogs.detailHowToClaimTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GamingDarkSurfaceVariant)
                                    .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(item.instructions.replace("\r", ""), fontSize = 13.sp, color = TextPrimary, lineHeight = 19.sp)
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        MetacriticDetailSection(info = item.metacriticInfo, gameTitle = item.title, context = context)
                        Spacer(modifier = Modifier.height(18.dp))
                        OfficialOstSection(ostInfo = ostInfo, modifier = Modifier.fillMaxWidth(), isLoading = isOstLoading)
                        Spacer(modifier = Modifier.height(18.dp))
                        PublicReviewsSection(
                            item = item, reviews = reviews, defaultAuthorName = defaultAuthorName,
                            onPostReview = { rating, title, comment -> onPostReview?.invoke(rating, title, comment) },
                            onVoteHelpful = { onVoteHelpful?.invoke(it) },
                            hasVotedHelpful = { hasVotedHelpful?.invoke(it) ?: false }
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        Text(s.dialogs.detailDescriptionTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(item.description, fontSize = 14.sp, color = TextPrimary, lineHeight = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        if (item.categories.isNotEmpty()) {
                            Text(
                                when (lang) {
                                    AppLanguage.SPANISH -> "CATEGORÍAS Y GÉNEROS"
                                    AppLanguage.FRENCH -> "CATÉGORIES ET GENRES"
                                    AppLanguage.ENGLISH -> "SUPPORTED CATEGORIES & GENRES"
                                },
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
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(category.colorHex).copy(alpha = 0.18f))
                                                .border(1.dp, Color(category.colorHex).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("${category.iconEmoji} ${category.localizedDisplayName(s)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(category.colorHex))
                                        }
                                        Text(category.localizedDescription(s), fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        DetailHardwareReadinessSection(item = item) {
                            storageInputText = item.manualInstallSizeGb?.toString() ?: ""
                            isStorageDialogOpen = true
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailMetadataBox(item = item, isOstLoading = isOstLoading, ostInfo = ostInfo)
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    if (showShareCardPreview) ShareCardPreviewDialog(item = item, onDismiss = { showShareCardPreview = false })

    if (isStorageDialogOpen) {
        DetailStorageFootprintDialog(
            item = item,
            storageInputText = storageInputText,
            onInputChange = { storageInputText = it },
            onDismiss = { isStorageDialogOpen = false },
            onSave = { parsed ->
                onUpdateManualStorage?.invoke(parsed)
                isStorageDialogOpen = false
            }
        )
    }
}
