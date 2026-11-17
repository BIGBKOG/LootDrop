package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherType
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedDisplayName
import com.example.ui.GiveawaysViewModel

@Composable
fun GeneralSettingsCategoryContent(
    viewModel: GiveawaysViewModel
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val publicReviews by viewModel.publicReviews.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val defaultLauncher by viewModel.defaultLauncher.collectAsState()
    val isShowOriginalPrice by viewModel.isShowOriginalPrice.collectAsState()
    val isShowExpiration by viewModel.isShowExpiration.collectAsState()
    val isConfirmToastOnClaim by viewModel.isConfirmToastOnClaim.collectAsState()
    val isUseInAppBrowser by viewModel.isUseInAppBrowser.collectAsState()
    val isAutoCheckOnReopen by viewModel.isAutoCheckOnReopen.collectAsState()
    val isAutoCheckGithubUpdates by viewModel.isAutoCheckGithubUpdates.collectAsState()
    val isAnimationsEnabled by viewModel.isAnimationsEnabled.collectAsState()
    val targetFrameRate by viewModel.targetFrameRate.collectAsState()
    val detectedRefreshRate by viewModel.detectedRefreshRate.collectAsState()
    val isLowEndOptimization by viewModel.isLowEndOptimization.collectAsState()
    val appUpdateInfo by viewModel.appUpdateInfo.collectAsState()
    val isCheckingUpdates by viewModel.isCheckingUpdates.collectAsState()
    val updateCheckStatus by viewModel.updateCheckStatus.collectAsState()
    val updateDownloadState by viewModel.updateDownloadState.collectAsState()

    SectionHeader(title = s.settings.languageSectionTitle, icon = Icons.Default.Language)
    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("settings_language_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = s.settings.languageCardSubtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
        listOf(
            Triple(AppLanguage.ENGLISH, s.settings.languageEnglishDesc, "settings_language_en"),
            Triple(AppLanguage.SPANISH, s.settings.languageSpanishDesc, "settings_language_es")
        ).forEach { (language, desc, tag) ->
            val isSelected = appLanguage == language
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.setAppLanguage(language) }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .testTag(tag),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = language.flagEmoji, fontSize = 20.sp)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = language.nativeName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = s.settings.languageActiveBadge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Text(
                            text = desc,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = language.nativeName,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    SectionHeader(title = s.settings.generalReviewsSection, icon = Icons.Default.Star)
    Spacer(modifier = Modifier.height(8.dp))

    LootDropAppRatingCard(
        reviews = publicReviews,
        defaultAuthorName = userProfile.name,
        onPostAppReview = { rating, title, comment ->
            viewModel.postAppReview(rating = rating, title = title, comment = comment) { _, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        },
        onVoteHelpful = { viewModel.voteReviewHelpful(it) },
        hasVotedHelpful = { viewModel.hasVotedHelpful(it) }
    )

    Spacer(modifier = Modifier.height(20.dp))

    SectionHeader(title = s.settings.generalDefaultLauncherSection, icon = Icons.Default.SportsEsports)
    Spacer(modifier = Modifier.height(8.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(s.settings.generalDefaultLauncherPrompt, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            listOf(
                LauncherType.ALL, LauncherType.PLAYSTATION, LauncherType.XBOX,
                LauncherType.NINTENDO, LauncherType.ANDROID, LauncherType.IOS,
                LauncherType.STEAM, LauncherType.EPIC, LauncherType.GOG
            ).forEach { launcher ->
                val isSelected = launcher == defaultLauncher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.setDefaultLauncher(launcher) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(launcher.badgeColorHex)))
                        Text(
                            text = launcher.localizedDisplayName(s),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    SectionHeader(title = s.settings.generalDisplayBehaviorSection, icon = Icons.Default.Tune)
    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        SimpleSettingSwitchRow(
            title = s.settings.generalShowWorthTitle,
            subtitle = s.settings.generalShowWorthSub,
            checked = isShowOriginalPrice,
            onCheckedChange = { viewModel.setShowOriginalPrice(it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

        SimpleSettingSwitchRow(
            title = s.settings.generalShowExpTitle,
            subtitle = s.settings.generalShowExpSub,
            checked = isShowExpiration,
            onCheckedChange = { viewModel.setShowExpiration(it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

        SimpleSettingSwitchRow(
            title = s.settings.generalClaimToastTitle,
            subtitle = s.settings.generalClaimToastSub,
            checked = isConfirmToastOnClaim,
            onCheckedChange = { viewModel.setConfirmToastOnClaim(it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

        SimpleSettingSwitchRow(
            title = s.settings.generalInAppBrowserTitle,
            subtitle = s.settings.generalInAppBrowserSub,
            checked = isUseInAppBrowser,
            switchModifier = Modifier.testTag("switch_use_in_app_browser"),
            onCheckedChange = { viewModel.setUseInAppBrowser(it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

        SimpleSettingSwitchRow(
            title = s.settings.generalAutoReopenTitle,
            subtitle = s.settings.generalAutoReopenSub,
            checked = isAutoCheckOnReopen,
            switchModifier = Modifier.testTag("switch_auto_check_on_reopen"),
            onCheckedChange = { viewModel.setAutoCheckOnReopen(it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

        SimpleSettingSwitchRow(
            title = s.settings.generalAutoGithubTitle,
            subtitle = s.settings.generalAutoGithubSub,
            checked = isAutoCheckGithubUpdates,
            switchModifier = Modifier.testTag("switch_auto_check_github_updates"),
            onCheckedChange = { viewModel.setAutoCheckGithubUpdates(it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

        SimpleSettingSwitchRow(
            title = s.settings.generalFluidAnimTitle,
            subtitle = s.settings.generalFluidAnimSub,
            checked = isAnimationsEnabled,
            switchModifier = Modifier.testTag("switch_ui_animations"),
            onCheckedChange = { viewModel.setAnimationsEnabled(it) }
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    PreRenderingPerformanceSection(
        targetFrameRate = targetFrameRate,
        detectedRefreshRate = detectedRefreshRate,
        isLowEndOptimization = isLowEndOptimization,
        onToggleLowEndOptimization = { viewModel.setLowEndOptimization(it) },
        onSelectFrameRate = { viewModel.setTargetFrameRate(it) }
    )

    Spacer(modifier = Modifier.height(20.dp))

    SectionHeader(title = s.settings.generalTourSection, icon = Icons.Default.AutoAwesome)
    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(s.settings.generalTourTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = s.settings.generalTourSub,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        Button(
            onClick = { viewModel.replayOnboarding() },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("replay_onboarding_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background
            )
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("  ${s.settings.generalReplayTourBtn}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    GithubUpdatesSection(
        appUpdateInfo = appUpdateInfo,
        isCheckingUpdates = isCheckingUpdates,
        updateCheckStatus = updateCheckStatus,
        updateDownloadState = updateDownloadState,
        viewModel = viewModel,
        context = context
    )
}
