package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.UserProfile
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import java.io.File

/**
 * Circular profile avatar button displayed on the Home page top-left header.
 * Shows the user's custom photo if set, or their gamer initial/icon with a cyber glow ring.
 */
@Composable
fun ProfileAvatarCircle(
    profile: UserProfile,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val hasCustomAvatar = profile.hasCustomAvatar
    val avatarFile = remember(profile.avatarPath) {
        if (profile.avatarPath.isNotBlank()) File(profile.avatarPath) else null
    }

    Box(
        modifier = modifier
            .size(maxOf(48.dp, size))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(bounded = false, radius = (size / 2) + 6.dp),
                role = Role.Button,
                onClickLabel = "Open Profile for ${profile.displayName}",
                onClick = onClick
            )
            .testTag("home_profile_avatar_circle"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(
                    width = 1.5.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            NeonCyan,
                            NeonGreen,
                            NeonCyan
                        )
                    ),
                    shape = CircleShape
                )
                .background(Color(0xFF10141E)),
            contentAlignment = Alignment.Center
        ) {
            if (hasCustomAvatar && avatarFile != null && avatarFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(avatarFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Photo of ${profile.displayName}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else if (profile.name.isNotBlank()) {
                // Initial Letter
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.initial,
                        fontSize = (size.value * 0.44f).sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan
                    )
                }
            } else {
                // Default Gamer Icon
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF172033),
                                    Color(0xFF0B0F19)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Create Profile",
                        tint = NeonCyan,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
        }
    }
}
