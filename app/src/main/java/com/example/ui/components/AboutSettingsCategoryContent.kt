package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen

private data class CreditEntry(
    val title: String,
    val role: String,
    val description: String,
    val icon: ImageVector,
    val badge: String? = null,
    val url: String? = null,
    val testTag: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutSettingsCategoryContent() {
    val context = LocalContext.current

    // SECTION 1: App Overview & Identity Hero Card
    SectionHeader(title = "ABOUT LOOTDROP", icon = Icons.Default.Info)

    Spacer(modifier = Modifier.height(8.dp))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("about_hero_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "LootDrop App Icon",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "LootDrop",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "V${BuildConfig.VERSION_NAME}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Build ${BuildConfig.VERSION_CODE} • Free Games & 100% Discount Radar",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Built to track 100% free games, limited-time DLC giveaways, Steam library & achievements, OSTs, and weekly backlog challenges with zero ads and zero tracking.",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AboutPillBadge(text = "Pure AOSP", accentColor = NeonGreen)
                AboutPillBadge(text = "Zero Ads", accentColor = MaterialTheme.colorScheme.primary)
                AboutPillBadge(text = "Open Source", accentColor = NeonCyan)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { openExternalUrl(context, "https://github.com/BIGBKOG/LootDrop") },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("about_github_repo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GitHub Repo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = { openExternalUrl(context, "https://bigbkog.github.io/LootDrop/") },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("about_github_pages_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Web Edition",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION 2: Creator & Contributors
    SectionHeader(title = "CREATOR & CONTRIBUTORS", icon = Icons.Default.Groups)

    Spacer(modifier = Modifier.height(8.dp))

    val contributors = listOf(
        CreditEntry(
            title = "BIGBKOG",
            role = "Creator, Lead Developer & Maintainer",
            description = "Original concept, Kotlin/Jetpack Compose Android app, GitHub Pages Web Edition, release management & feature design.",
            icon = Icons.Default.Person,
            badge = "CREATOR",
            url = "https://github.com/BIGBKOG",
            testTag = "credit_contributor_bigbkog"
        ),
        CreditEntry(
            title = "Community Contributors & Patreon Supporters",
            role = "Testers, Reviewers & Backers",
            description = "Everyone testing builds, reporting bugs, submitting community reviews, and supporting LootDrop on Patreon.",
            icon = Icons.Default.Favorite,
            badge = "COMMUNITY",
            url = "https://www.patreon.com/cw/LootDrop",
            testTag = "credit_contributor_community"
        )
    )

    CreditsGroupCard(
        entries = contributors,
        cardTestTag = "about_contributors_card",
        onItemClick = { url -> openExternalUrl(context, url) }
    )

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION 3: Data APIs & External Services
    SectionHeader(title = "POWERED BY APIS & SERVICES", icon = Icons.Default.Api)

    Spacer(modifier = Modifier.height(8.dp))

    val apiCredits = listOf(
        CreditEntry(
            title = "GamerPower API",
            role = "Primary Giveaways & Free Loot Feed",
            description = "Real-time 100% free game, DLC, and loot drop tracking across PC, Steam, Epic Games, GOG, PlayStation, Xbox & mobile.",
            icon = Icons.Default.SportsEsports,
            badge = "CORE API",
            url = "https://www.gamerpower.com/api-read",
            testTag = "credit_api_gamerpower"
        ),
        CreditEntry(
            title = "CheapShark API",
            role = "Storefront Deals & Price Comparisons",
            description = "Cross-store price lookups, retail savings comparisons, and deal tracking across major digital PC storefronts.",
            icon = Icons.Default.LocalOffer,
            badge = "DEALS API",
            url = "https://apidocs.cheapshark.com/",
            testTag = "credit_api_cheapshark"
        ),
        CreditEntry(
            title = "Steam Web & Storefront API",
            role = "Library Sync, Achievements & DLC Radar",
            description = "Owned Steam games lookup, global & personal achievement percentages, wishlists, and DLC recommendations.",
            icon = Icons.Default.Storefront,
            badge = "STEAM",
            url = "https://store.steampowered.com/",
            testTag = "credit_api_steam"
        ),
        CreditEntry(
            title = "MusicBrainz, Discogs & Spotify APIs",
            role = "Official Soundtrack (OST) Discovery",
            description = "Album metadata, tracklists, release years, and soundtrack streaming links for games in your collection.",
            icon = Icons.Default.Album,
            badge = "OST DATA",
            url = "https://musicbrainz.org/",
            testTag = "credit_api_ost"
        ),
        CreditEntry(
            title = "Cloudflare Workers / R2 & Supabase",
            role = "Community Reviews & Optional Vault Sync",
            description = "Public community reviews storage and optional encrypted account backup sync across devices.",
            icon = Icons.Default.CloudQueue,
            badge = "CLOUD",
            url = "https://www.cloudflare.com/",
            testTag = "credit_api_cloud"
        ),
        CreditEntry(
            title = "GitHub Releases & Pages",
            role = "In-App Updater & Web App Hosting",
            description = "Public release feed checking, APK distribution, and hosting for the LootDrop Web Edition.",
            icon = Icons.Default.Verified,
            badge = "UPDATES",
            url = "https://github.com/BIGBKOG/LootDrop/releases",
            testTag = "credit_api_github"
        )
    )

    CreditsGroupCard(
        entries = apiCredits,
        cardTestTag = "about_apis_card",
        onItemClick = { url -> openExternalUrl(context, url) }
    )

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION 4: Open-Source Libraries & License
    SectionHeader(title = "TECH STACK & OPEN SOURCE", icon = Icons.Default.Layers)

    Spacer(modifier = Modifier.height(8.dp))

    val techStack = listOf(
        CreditEntry(
            title = "Kotlin & Jetpack Compose (Material 3)",
            role = "Language & Declarative UI",
            description = "Material 3 Expressive components, edge-to-edge system bars, adaptive layouts, and custom font theming.",
            icon = Icons.Default.Code,
            testTag = "credit_tech_compose"
        ),
        CreditEntry(
            title = "AndroidX Room, Lifecycle & Coroutines",
            role = "Local Storage & Reactive Architecture",
            description = "Offline giveaway caching, vault persistence, home screen widgets, and lifecycle-safe state flows.",
            icon = Icons.Default.Layers,
            testTag = "credit_tech_androidx"
        ),
        CreditEntry(
            title = "Retrofit, OkHttp, Moshi & Coil",
            role = "Networking, JSON & Artwork Loading",
            description = "Resilient REST API requests, disk HTTP caching, Kotlin JSON parsing, and fast cover image rendering.",
            icon = Icons.Default.Api,
            testTag = "credit_tech_networking"
        ),
        CreditEntry(
            title = "Open-Source Project License",
            role = "Repository Licensing & Terms",
            description = "View the LootDrop repository LICENSE file and third-party open-source attributions on GitHub.",
            icon = Icons.Default.Gavel,
            badge = "LICENSE",
            url = "https://github.com/BIGBKOG/LootDrop/blob/main/LICENSE",
            testTag = "credit_tech_license"
        )
    )

    CreditsGroupCard(
        entries = techStack,
        cardTestTag = "about_tech_stack_card",
        onItemClick = { url -> openExternalUrl(context, url) }
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedButton(
        onClick = { openExternalUrl(context, "https://www.patreon.com/cw/LootDrop") },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("about_support_patreon_button")
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Color(0xFFFF424D),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Support BIGBKOG & LootDrop on Patreon",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CreditsGroupCard(
    entries: List<CreditEntry>,
    cardTestTag: String,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .testTag(cardTestTag)
    ) {
        entries.forEachIndexed { index, entry ->
            val rowModifier = if (entry.url != null) {
                Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(entry.url) }
                    .minimumInteractiveComponentSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag(entry.testTag)
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag(entry.testTag)
            }

            Row(
                modifier = rowModifier,
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                        .border(
                            0.8.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = entry.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = entry.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (entry.badge != null) {
                            AboutPillBadge(
                                text = entry.badge,
                                accentColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = entry.role,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = entry.description,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (entry.url != null) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open ${entry.title}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(16.dp)
                    )
                }
            }

            if (index < entries.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun AboutPillBadge(
    text: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accentColor.copy(alpha = 0.14f))
            .border(0.6.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor
        )
    }
}

private fun openExternalUrl(context: Context, url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
    }
}
