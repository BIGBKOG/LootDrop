package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.theme.ExpressiveMotion
import com.example.ui.theme.rememberExpressiveCornerRadius
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AlertsSettingsCategoryContent
import com.example.ui.components.AppearanceSettingsCategoryContent
import com.example.ui.components.GeneralSettingsCategoryContent
import com.example.ui.components.SectionHeader
import com.example.ui.components.SettingsFooter
import com.example.ui.components.StorageSettingsCategoryContent

@Composable
fun SettingsScreen(
    viewModel: GiveawaysViewModel,
    scrollState: ScrollState = rememberScrollState(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAnimationsEnabled by viewModel.isAnimationsEnabled.collectAsState()
    val isLowEndOptimization by viewModel.isLowEndOptimization.collectAsState()
    val reducedMotion = !isAnimationsEnabled || isLowEndOptimization

    var selectedCategory by rememberSaveable { mutableStateOf(SettingsCategory.APPEARANCE) }
    var showClearClaimedDialog by remember { mutableStateOf(false) }
    var showResyncDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCategory) {
        scrollState.animateScrollTo(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("settings_screen")
    ) {
        // Section: Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "Settings & Customization",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Personalize theme modes, alert rules & app preferences",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Categorized Settings Tabs Bar
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_category_tabs")
        ) {
            items(SettingsCategory.values()) { category ->
                val isSelected = category == selectedCategory
                val tabInteraction = remember { MutableInteractionSource() }
                val isPressed by tabInteraction.collectIsPressedAsState()
                val tabScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else if (isSelected) 1.02f else 1f,
                    animationSpec = ExpressiveMotion.spatialFast(reducedMotion),
                    label = "settings_tab_scale_${category.name}"
                )
                val tabCorner = rememberExpressiveCornerRadius(
                    isSelected = isSelected,
                    isPressed = isPressed,
                    baseRadius = 12.dp,
                    selectedRadius = 16.dp,
                    pressedRadius = 20.dp,
                    enabled = !reducedMotion
                )
                val tabBg by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    animationSpec = ExpressiveMotion.effectsDefault(reducedMotion),
                    label = "settings_tab_bg_${category.name}"
                )
                Surface(
                    onClick = { selectedCategory = category },
                    interactionSource = tabInteraction,
                    shape = RoundedCornerShape(tabCorner),
                    color = tabBg,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .graphicsLayer {
                            scaleX = tabScale
                            scaleY = tabScale
                        }
                        .testTag("settings_tab_${category.name.lowercase()}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = category.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Category Description Pill
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                AnimatedContent(
                    targetState = selectedCategory.description,
                    transitionSpec = { ExpressiveMotion.popNumberTransform(reducedMotion) },
                    label = "settings_category_desc"
                ) { desc ->
                    Text(
                        text = desc,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content with directional M3 Expressive slide & fade
        AnimatedContent(
            targetState = selectedCategory,
            transitionSpec = {
                val forward = targetState.ordinal >= initialState.ordinal
                ExpressiveMotion.directionalTabSlideTransform(forward = forward, reducedMotion = reducedMotion)
            },
            label = "settings_category_content"
        ) { category ->
            Column(modifier = Modifier.fillMaxWidth()) {
                when (category) {
                    SettingsCategory.APPEARANCE -> {
                        AppearanceSettingsCategoryContent(viewModel = viewModel)
                    }
                    SettingsCategory.ALERTS -> {
                        SectionHeader(title = "PUSH NOTIFICATIONS & FREEBIE ALERTS", icon = Icons.Default.Settings)
                        Spacer(modifier = Modifier.height(8.dp))
                        AlertsSettingsCategoryContent(viewModel = viewModel)
                    }
                    SettingsCategory.GENERAL -> {
                        GeneralSettingsCategoryContent(viewModel = viewModel)
                    }
                    SettingsCategory.STORAGE -> {
                        StorageSettingsCategoryContent(
                            viewModel = viewModel,
                            onRequestResyncDialog = { showResyncDialog = true },
                            onRequestClearClaimedDialog = { showClearClaimedDialog = true }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // App Info Footer - Always visible at bottom of all settings tabs
        SettingsFooter()

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Confirmation Dialogs
    if (showClearClaimedDialog) {
        AlertDialog(
            onDismissRequest = { showClearClaimedDialog = false },
            title = { Text("Clear Claimed Games?") },
            text = { Text("This will remove all games from your 'In Library' tab. You can still re-mark them at any time.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearClaimedGames()
                        showClearClaimedDialog = false
                        Toast.makeText(context, "Claimed library cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearClaimedDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showResyncDialog) {
        AlertDialog(
            onDismissRequest = { showResyncDialog = false },
            title = { Text("Re-sync Store Freebies?") },
            text = { Text("This refreshes all giveaways directly from Steam, Epic, GOG, and other launchers.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.forceResync()
                        showResyncDialog = false
                        Toast.makeText(context, "Refreshing all giveaways...", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Re-sync", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResyncDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
