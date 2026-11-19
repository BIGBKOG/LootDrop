package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserProfile
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.density.LocalScreenDensityInfo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val badge: String,
    val icon: ImageVector,
    val tags: List<String>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingWalkthroughDialog(
    initialProfile: UserProfile = UserProfile(),
    currentLanguage: AppLanguage = LocalAppStrings.current.language,
    onLanguageChange: ((AppLanguage) -> Unit)? = null,
    onSaveProfile: ((name: String, avatarUri: Uri?, removeAvatar: Boolean) -> Unit)? = null,
    onDismiss: () -> Unit,
    onFinish: () -> Unit
) {
    val s = LocalAppStrings.current
    val steps = remember(s) { defaultOnboardingSteps(s) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val totalSteps = steps.size
    val screenDensityInfo = LocalScreenDensityInfo.current
    val isCompactHeight = screenDensityInfo.isCompactHeight
    val isUltraCompactHeight = screenDensityInfo.isUltraCompactHeight
    val isSmallPhone = screenDensityInfo.isNarrowScreen || screenDensityInfo.widthDp < 380
    val animBoxSize = screenDensityInfo.walkthroughAnimBoxSize
    val buttonHeight = screenDensityInfo.walkthroughButtonHeight

    var nameInput by remember(initialProfile.name) { mutableStateOf(initialProfile.name) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var shouldRemoveAvatar by remember { mutableStateOf(false) }

    val avatarFile = remember(initialProfile.avatarPath) {
        if (initialProfile.avatarPath.isNotBlank()) File(initialProfile.avatarPath) else null
    }
    val saveProfile = { onSaveProfile?.invoke(nameInput.trim(), selectedImageUri, shouldRemoveAvatar) }
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            shouldRemoveAvatar = false
            onSaveProfile?.invoke(nameInput.trim(), uri, false)
        }
    }

    BackHandler {
        saveProfile()
        if (currentStepIndex > 0) currentStepIndex-- else onDismiss()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxSize().testTag("onboarding_walkthrough_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxHeight().widthIn(max = screenDensityInfo.maxDialogWidth).fillMaxWidth(),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    topBar = {
                        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 4.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = if (isSmallPhone) 12.dp else 18.dp, vertical = if (isCompactHeight) 4.dp else 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isCompactHeight) 28.dp else 32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(if (isCompactHeight) 16.dp else 18.dp)
                                        )
                                    }
                                    Text(s.dialogs.onboardingHeaderTitle, fontSize = if (isCompactHeight) 13.sp else 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .padding(horizontal = 4.dp, vertical = 1.5.dp)
                                    ) {
                                        Text("${screenDensityInfo.densityDpi} DPI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (onLanguageChange != null) {
                                        val nextLang = when (currentLanguage) {
                                            AppLanguage.ENGLISH -> AppLanguage.SPANISH
                                            AppLanguage.SPANISH -> AppLanguage.FRENCH
                                            AppLanguage.FRENCH -> AppLanguage.ENGLISH
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                                .clickable { onLanguageChange(nextLang) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .testTag("onboarding_lang_toggle")
                                        ) {
                                            Text(
                                                text = "${currentLanguage.flagEmoji} ${currentLanguage.shortBadge}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    TextButton(
                                        onClick = { saveProfile(); onFinish() },
                                        modifier = Modifier.testTag("onboarding_skip_button")
                                    ) {
                                        Text(s.common.skip, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = if (isCompactHeight) 4.dp else 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until totalSteps) {
                                    val isActive = i == currentStepIndex
                                    val isCompleted = i < currentStepIndex
                                    val pillWidth by animateDpAsState(
                                        targetValue = if (isActive) (if (isSmallPhone) 20.dp else 28.dp) else 8.dp,
                                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                                        label = "pillWidth"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .height(if (isCompactHeight) 6.dp else 8.dp)
                                            .width(pillWidth)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isActive -> MaterialTheme.colorScheme.primary
                                                    isCompleted -> NeonGreen
                                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                                }
                                            )
                                            .clickable { currentStepIndex = i }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 8.dp))
                        }
                    },
                    bottomBar = {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(
                                    start = if (isSmallPhone) 14.dp else 20.dp,
                                    end = if (isSmallPhone) 14.dp else 20.dp,
                                    top = if (isUltraCompactHeight) 8.dp else 12.dp,
                                    bottom = if (isUltraCompactHeight) 10.dp else if (isCompactHeight) 14.dp else 16.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (currentStepIndex > 0) {
                                    OutlinedButton(
                                        onClick = { currentStepIndex-- },
                                        modifier = Modifier.weight(1f).height(buttonHeight).testTag("onboarding_back_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.common.back, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(s.common.back, fontSize = if (isSmallPhone) 12.sp else 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                }

                                Button(
                                    onClick = {
                                        saveProfile()
                                        if (currentStepIndex < totalSteps - 1) currentStepIndex++ else onFinish()
                                    },
                                    modifier = Modifier.weight(if (currentStepIndex > 0) 1.5f else 1f).height(buttonHeight).testTag("onboarding_next_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentStepIndex == totalSteps - 1) NeonGreen else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = if (currentStepIndex == totalSteps - 1) s.dialogs.onboardingFinishCta else s.dialogs.onboardingNextStep,
                                        fontSize = if (isSmallPhone) 13.sp else 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B0F19)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (currentStepIndex == totalSteps - 1) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color(0xFF0B0F19),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        AnimatedContent(
                            targetState = currentStepIndex,
                            transitionSpec = {
                                val dir = if (targetState > initialState) 1 else -1
                                (slideInHorizontally(initialOffsetX = { dir * it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350)))
                                    .togetherWith(slideOutHorizontally(targetOffsetX = { -dir * it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350)))
                            },
                            modifier = Modifier.fillMaxSize(),
                            label = "onboarding_step_content"
                        ) { stepIdx ->
                            val step = steps[stepIdx]
                            val animScale = (animBoxSize.value / 135f).coerceIn(0.6f, 1.15f)
                            val hasCustomAvatar = selectedImageUri != null || (avatarFile != null && avatarFile.exists() && !shouldRemoveAvatar)

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = if (isSmallPhone) 14.dp else 20.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 2.dp else 6.dp))

                                Box(
                                    modifier = Modifier
                                        .size(animBoxSize)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = animScale; scaleY = animScale },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (stepIdx) {
                                            0 -> ProfileAvatarTourPreview(
                                                name = nameInput,
                                                selectedUri = selectedImageUri,
                                                avatarFile = avatarFile,
                                                shouldRemove = shouldRemoveAvatar,
                                                onPickPhoto = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                                            )
                                            1 -> RadarScanAnimation()
                                            2 -> DirectClaimAnimation()
                                            3 -> SavingsTrackerAnimation()
                                            4 -> AlertsAndThemesAnimation()
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 6.dp else if (isCompactHeight) 8.dp else 12.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = if (isUltraCompactHeight) 8.dp else 10.dp, vertical = if (isUltraCompactHeight) 2.dp else 4.dp)
                                ) {
                                    Text(
                                        text = step.badge,
                                        fontSize = if (isUltraCompactHeight) 9.5.sp else 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.2.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 4.dp else 8.dp))
                                Text(
                                    text = step.title,
                                    fontSize = if (isUltraCompactHeight) 17.sp else if (isCompactHeight) 19.sp else 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 2.dp else 4.dp))
                                Text(
                                    text = step.subtitle,
                                    fontSize = if (isUltraCompactHeight) 9.5.sp else if (isCompactHeight) 10.5.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 4.dp else 8.dp))
                                Text(
                                    text = step.description,
                                    fontSize = if (isUltraCompactHeight) 11.5.sp else if (isCompactHeight) 12.5.sp else 13.5.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = if (isUltraCompactHeight) 15.sp else if (isCompactHeight) 16.5.sp else 18.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 6.dp else 10.dp))

                                if (stepIdx == 0) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedButton(
                                            onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                            modifier = Modifier.testTag("onboarding_pick_photo_btn")
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (hasCustomAvatar) s.dialogs.onboardingChangePhoto else s.dialogs.onboardingAddPhoto, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        if (hasCustomAvatar) {
                                            TextButton(
                                                onClick = {
                                                    selectedImageUri = null
                                                    shouldRemoveAvatar = true
                                                    onSaveProfile?.invoke(nameInput.trim(), null, true)
                                                },
                                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                                                modifier = Modifier.testTag("onboarding_remove_photo_btn")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(s.common.remove, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = nameInput,
                                        onValueChange = {
                                            if (it.length <= 30) {
                                                nameInput = it
                                                onSaveProfile?.invoke(it.trim(), selectedImageUri, shouldRemoveAvatar)
                                            }
                                        },
                                        placeholder = { Text(s.dialogs.onboardingNamePlaceholder, color = TextMuted) },
                                        singleLine = true,
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp)) },
                                        modifier = Modifier.fillMaxWidth().testTag("onboarding_profile_name_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.spacedBy(if (isUltraCompactHeight) 4.dp else 6.dp)
                                ) {
                                    step.tags.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 3.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                                .padding(
                                                    horizontal = if (isUltraCompactHeight) 7.dp else 9.dp,
                                                    vertical = if (isUltraCompactHeight) 3.dp else 5.dp
                                                )
                                        ) {
                                            Text(
                                                text = tag,
                                                fontSize = if (isUltraCompactHeight) 10.5.sp else 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 10.dp else 24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
