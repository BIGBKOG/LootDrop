package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tour
import androidx.compose.material.icons.filled.Tune
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import com.example.data.local.SteamAchievementEntity
import com.example.data.model.TrophyRarityTier
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.UserProfile
import com.example.ui.UiStats
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

@Composable
fun ProfileDialog(
    profile: UserProfile,
    uiStats: UiStats,
    steamUserId: String = "",
    steamTrophies: List<SteamAchievementEntity> = emptyList(),
    isSyncingSteam: Boolean = false,
    onSyncSteam: (() -> Unit)? = null,
    onOpenSteamDialog: (() -> Unit)? = null,
    onSaveProfile: (name: String, avatarUri: Uri?, removeAvatar: Boolean) -> Unit,
    onSaveDisplayedTrophies: ((hasCustom: Boolean, keys: Set<String>) -> Unit)? = null,
    onReplayTour: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var nameInput by remember(profile.name) { mutableStateOf(profile.name) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var shouldRemoveAvatar by remember { mutableStateOf(false) }
    var showTrophyChooser by remember { mutableStateOf(false) }

    val displayedTrophies = remember(profile.hasCustomTrophySelection, profile.displayedTrophyKeys, steamTrophies) {
        if (profile.hasCustomTrophySelection) {
            steamTrophies.filter { "${it.appId}_${it.apiName}" in profile.displayedTrophyKeys }
        } else {
            steamTrophies.take(15)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            shouldRemoveAvatar = false
        }
    }

    val avatarFile = remember(profile.avatarPath) {
        if (profile.avatarPath.isNotBlank()) File(profile.avatarPath) else null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .testTag("gamer_profile_dialog"),
            color = Color(0xFF131722),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Gamer Profile",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Stored locally & in JSON backups",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).testTag("profile_dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Avatar Photo Section
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            Brush.sweepGradient(listOf(NeonCyan, NeonGreen, NeonCyan)),
                            CircleShape
                        )
                        .background(Color(0xFF0F121A)),
                    contentAlignment = Alignment.Center
                ) {
                    val hasPendingUri = selectedImageUri != null
                    val hasExistingFile = !shouldRemoveAvatar && avatarFile != null && avatarFile.exists()

                    if (hasPendingUri) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(selectedImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else if (hasExistingFile) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarFile)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else if (nameInput.isNotBlank()) {
                        Text(
                            text = nameInput.trim().take(1).uppercase(),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Avatar",
                            tint = NeonCyan.copy(alpha = 0.8f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Photo actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        modifier = Modifier.testTag("profile_pick_photo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedImageUri != null || (avatarFile != null && avatarFile.exists())) "Change Photo" else "Add Photo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (selectedImageUri != null || (avatarFile != null && avatarFile.exists() && !shouldRemoveAvatar)) {
                        TextButton(
                            onClick = {
                                selectedImageUri = null
                                shouldRemoveAvatar = true
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                            modifier = Modifier.testTag("profile_remove_photo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Remove", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Name TextField
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "GAMER TAG / NAME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { if (it.length <= 30) nameInput = it },
                        placeholder = { Text("e.g. CyberValkyrie", color = TextMuted) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0xFF282E40),
                            focusedContainerColor = Color(0xFF10131B),
                            unfocusedContainerColor = Color(0xFF10131B),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Player Stats Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1219)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222736))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiStats.claimedCount}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                            Text(
                                text = "Vault Games",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF222736)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(java.util.Locale.US, "$%.2f", uiStats.claimedTotalSavedUsd),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGold
                            )
                            Text(
                                text = "Dollars Saved",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF222736)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiStats.steamTotalCount}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                            Text(
                                text = "Steam Games",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Steam Trophies Showcase Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_steam_trophies_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F131F)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF252D42))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Section Header
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
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "STEAM TROPHIES",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700),
                                    letterSpacing = 0.5.sp
                                )
                            }

                            if (steamUserId.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isSyncingSteam) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 1.5.dp,
                                            color = NeonCyan
                                        )
                                    }

                                    if (steamTrophies.isNotEmpty() && onSaveDisplayedTrophies != null) {
                                        TextButton(
                                            onClick = { showTrophyChooser = true },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.testTag("profile_choose_trophies_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = NeonCyan,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = if (profile.hasCustomTrophySelection) "Edit (${displayedTrophies.size})" else "Choose",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NeonCyan
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NeonCyan.copy(alpha = 0.15f))
                                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (steamTrophies.isNotEmpty()) {
                                                if (profile.hasCustomTrophySelection) "${displayedTrophies.size} Displayed" else "${steamTrophies.size} Unlocked"
                                            } else "Linked",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                    }

                                    if (onSyncSteam != null) {
                                        IconButton(
                                            onClick = onSyncSteam,
                                            modifier = Modifier.size(24.dp).testTag("profile_sync_trophies_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Sync,
                                                contentDescription = "Sync Steam Trophies",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (steamUserId.isNotBlank()) {
                            if (steamTrophies.isNotEmpty()) {
                                // Status bar: Rarity breakdown chips and showcase mode
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val mythicCount = displayedTrophies.count { it.rarityTier == TrophyRarityTier.MYTHIC }
                                        val goldCount = displayedTrophies.count { it.rarityTier == TrophyRarityTier.GOLD }
                                        val silverCount = displayedTrophies.count { it.rarityTier == TrophyRarityTier.SILVER }

                                        if (mythicCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0x33E040FB))
                                                    .border(1.dp, Color(0xFFE040FB).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "💎 $mythicCount Mythic",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFFE040FB)
                                                )
                                            }
                                        }
                                        if (goldCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0x33FFD700))
                                                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "🥇 $goldCount Gold",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFFFFD700)
                                                )
                                            }
                                        }
                                        if (silverCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0x33CFD8DC))
                                                    .border(1.dp, Color(0xFFCFD8DC).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "🥈 $silverCount Silver",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFCFD8DC)
                                                )
                                            }
                                        }
                                    }

                                    if (profile.hasCustomTrophySelection) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NeonGreen.copy(alpha = 0.15f))
                                                .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "⭐ Custom Showcase",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NeonGreen
                                            )
                                        }
                                    }
                                }

                                if (displayedTrophies.isEmpty()) {
                                    // Empty showcase state when user has unselected all trophies
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF0F1422))
                                            .border(1.dp, Color(0xFF222B3F), RoundedCornerShape(12.dp))
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "No trophies chosen for your showcase",
                                                fontSize = 12.sp,
                                                color = TextSecondary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            OutlinedButton(
                                                onClick = { showTrophyChooser = true },
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.7f)),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                                modifier = Modifier.height(32.dp).testTag("profile_empty_choose_btn")
                                            ) {
                                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "Choose Trophies", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                } else {
                                    // Scrollable trophies cards
                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(displayedTrophies, key = { "prof_trophy_${it.appId}_${it.apiName}" }) { trophy ->
                                            ProfileTrophyCard(trophy = trophy)
                                        }
                                    }
                                }
                            } else {
                                // Linked but not synced yet
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Steam linked (${steamUserId.take(8)}...), but trophies haven't synced yet.",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                    if (onSyncSteam != null) {
                                        OutlinedButton(
                                            onClick = onSyncSteam,
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "Sync Trophies", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Steam not linked prompt
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Connect your Steam account to display your rarest trophies & unlocked achievements directly on your profile.",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 15.sp
                                )
                                if (onOpenSteamDialog != null) {
                                    OutlinedButton(
                                        onClick = {
                                            onDismiss()
                                            onOpenSteamDialog()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.7f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                        modifier = Modifier.height(36.dp).testTag("profile_link_steam_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SportsEsports,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Link Steam Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Save Profile Button
                Button(
                    onClick = {
                        val trimmed = nameInput.trim()
                        onSaveProfile(trimmed, selectedImageUri, shouldRemoveAvatar)
                        Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("profile_save_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Save Profile",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                // Replay Feature Tour Button
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onReplayTour()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("profile_replay_tour_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282E40)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tour,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Replay Feature Tour",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showTrophyChooser) {
        ChooseDisplayedTrophiesDialog(
            allTrophies = steamTrophies,
            hasCustomSelection = profile.hasCustomTrophySelection,
            currentSelectedKeys = profile.displayedTrophyKeys,
            onSave = { hasCustom, selectedKeys ->
                onSaveDisplayedTrophies?.invoke(hasCustom, selectedKeys)
            },
            onDismiss = { showTrophyChooser = false }
        )
    }
}

@Composable
private fun ProfileTrophyCard(trophy: SteamAchievementEntity) {
    val tier = trophy.rarityTier
    val tierColor = tier.color

    Box(
        modifier = Modifier
            .width(175.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF131826))
            .border(1.dp, tierColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Top row: Game name & rarity badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = trophy.gameName.ifBlank { "Steam Game" },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tier.containerColor)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${tier.iconEmoji} ${trophy.formattedRarity}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tierColor
                    )
                }
            }

            // Middle row: Icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(tierColor.copy(alpha = 0.15f))
                        .border(1.dp, tierColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (trophy.effectiveIconUrl.isNotBlank()) {
                        AsyncImage(
                            model = trophy.effectiveIconUrl,
                            contentDescription = trophy.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = tierColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = trophy.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (trophy.description.isNotBlank()) {
                Text(
                    text = trophy.description,
                    fontSize = 9.5.sp,
                    color = TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

