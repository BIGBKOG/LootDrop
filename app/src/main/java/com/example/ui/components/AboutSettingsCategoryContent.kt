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
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
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
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val context = LocalContext.current

    // SECTION 1: App Overview & Identity Hero Card
    SectionHeader(
        title = if (isEs) "ACERCA DE LOOTDROP" else "ABOUT LOOTDROP",
        icon = Icons.Default.Info
    )

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
                        text = if (isEs) {
                            "Compilación ${BuildConfig.VERSION_CODE} • Radar de Juegos Gratis y 100% Descuento"
                        } else {
                            "Build ${BuildConfig.VERSION_CODE} • Free Games & 100% Discount Radar"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = if (isEs) {
                    "Creado para rastrear juegos 100% gratis, DLCs por tiempo limitado, biblioteca y logros de Steam, bandas sonoras y retos semanales sin anuncios ni rastreadores."
                } else {
                    "Built to track 100% free games, limited-time DLC giveaways, Steam library & achievements, OSTs, and weekly backlog challenges with zero ads and zero tracking."
                },
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AboutPillBadge(text = if (isEs) "AOSP Puro" else "Pure AOSP", accentColor = NeonGreen)
                AboutPillBadge(text = if (isEs) "Cero Anuncios" else "Zero Ads", accentColor = MaterialTheme.colorScheme.primary)
                AboutPillBadge(text = if (isEs) "Código Abierto" else "Open Source", accentColor = NeonCyan)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { openExternalUrl(context, "https://github.com/BIGBKOG/LootDrop", isEs) },
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
                        text = if (isEs) "Repo GitHub" else "GitHub Repo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = { openExternalUrl(context, "https://bigbkog.github.io/LootDrop/", isEs) },
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
                        text = if (isEs) "Edición Web" else "Web Edition",
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
    SectionHeader(title = s.settings.aboutContributorsSection, icon = Icons.Default.Groups)

    Spacer(modifier = Modifier.height(8.dp))

    val contributors = listOf(
        CreditEntry(
            title = "BIGBKOG",
            role = if (isEs) "Creador, Desarrollador Principal y Mantenedor" else "Creator, Lead Developer & Maintainer",
            description = if (isEs) {
                "Concepto original, app Android en Kotlin/Jetpack Compose, Edición Web en GitHub Pages, gestión de versiones y diseño de funciones."
            } else {
                "Original concept, Kotlin/Jetpack Compose Android app, GitHub Pages Web Edition, release management & feature design."
            },
            icon = Icons.Default.Person,
            badge = if (isEs) "CREADOR" else "CREATOR",
            url = "https://github.com/BIGBKOG",
            testTag = "credit_contributor_bigbkog"
        ),
        CreditEntry(
            title = if (isEs) "Colaboradores de la Comunidad y Patreon" else "Community Contributors & Patreon Supporters",
            role = if (isEs) "Testers, Reseñadores y Patrocinadores" else "Testers, Reviewers & Backers",
            description = if (isEs) {
                "A todos los que prueban versiones, reportan errores, escriben reseñas en la comunidad y apoyan a LootDrop en Patreon."
            } else {
                "Everyone testing builds, reporting bugs, submitting community reviews, and supporting LootDrop on Patreon."
            },
            icon = Icons.Default.Favorite,
            badge = if (isEs) "COMUNIDAD" else "COMMUNITY",
            url = "https://www.patreon.com/cw/LootDrop",
            testTag = "credit_contributor_community"
        )
    )

    CreditsGroupCard(
        entries = contributors,
        cardTestTag = "about_contributors_card",
        isEs = isEs,
        onItemClick = { url -> openExternalUrl(context, url, isEs) }
    )

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION 3: Data APIs & External Services
    SectionHeader(title = s.settings.aboutApisSection, icon = Icons.Default.Api)

    Spacer(modifier = Modifier.height(8.dp))

    val apiCredits = listOf(
        CreditEntry(
            title = "GamerPower API",
            role = if (isEs) "Feed Principal de Regalos y Botín Gratis" else "Primary Giveaways & Free Loot Feed",
            description = if (isEs) {
                "Rastreo en tiempo real de juegos, DLCs y recompensas 100% gratis en PC, Steam, Epic Games, GOG, PlayStation, Xbox y móvil."
            } else {
                "Real-time 100% free game, DLC, and loot drop tracking across PC, Steam, Epic Games, GOG, PlayStation, Xbox & mobile."
            },
            icon = Icons.Default.SportsEsports,
            badge = if (isEs) "API PRINCIPAL" else "CORE API",
            url = "https://www.gamerpower.com/api-read",
            testTag = "credit_api_gamerpower"
        ),
        CreditEntry(
            title = "CheapShark API",
            role = if (isEs) "Ofertas de Tiendas y Comparación de Precios" else "Storefront Deals & Price Comparisons",
            description = if (isEs) {
                "Búsqueda de precios entre tiendas, comparativa de ahorro en tienda y seguimiento de ofertas en tiendas digitales de PC."
            } else {
                "Cross-store price lookups, retail savings comparisons, and deal tracking across major digital PC storefronts."
            },
            icon = Icons.Default.LocalOffer,
            badge = if (isEs) "API OFERTAS" else "DEALS API",
            url = "https://apidocs.cheapshark.com/",
            testTag = "credit_api_cheapshark"
        ),
        CreditEntry(
            title = if (isEs) "API Web y Tienda de Steam" else "Steam Web & Storefront API",
            role = if (isEs) "Sincronización de Biblioteca, Logros y Radar DLC" else "Library Sync, Achievements & DLC Radar",
            description = if (isEs) {
                "Consulta de juegos de Steam, porcentajes globales y personales de logros, lista de deseados y recomendaciones de DLC."
            } else {
                "Owned Steam games lookup, global & personal achievement percentages, wishlists, and DLC recommendations."
            },
            icon = Icons.Default.Storefront,
            badge = "STEAM",
            url = "https://store.steampowered.com/",
            testTag = "credit_api_steam"
        ),
        CreditEntry(
            title = if (isEs) "APIs de MusicBrainz, Discogs y Spotify" else "MusicBrainz, Discogs & Spotify APIs",
            role = if (isEs) "Descubrimiento de Bandas Sonoras (OST)" else "Official Soundtrack (OST) Discovery",
            description = if (isEs) {
                "Metadatos de álbumes, listas de pistas, años de lanzamiento y enlaces de reproducción de OST para tu colección."
            } else {
                "Album metadata, tracklists, release years, and soundtrack streaming links for games in your collection."
            },
            icon = Icons.Default.Album,
            badge = if (isEs) "DATOS OST" else "OST DATA",
            url = "https://musicbrainz.org/",
            testTag = "credit_api_ost"
        ),
        CreditEntry(
            title = if (isEs) "Cloudflare Workers / R2 y Supabase" else "Cloudflare Workers / R2 & Supabase",
            role = if (isEs) "Reseñas Públicas y Respaldo en la Nube" else "Community Reviews & Optional Vault Sync",
            description = if (isEs) {
                "Almacenamiento de reseñas de la comunidad y sincronización opcional de respaldo de cuenta entre dispositivos."
            } else {
                "Public community reviews storage and optional encrypted account backup sync across devices."
            },
            icon = Icons.Default.CloudQueue,
            badge = if (isEs) "NUBE" else "CLOUD",
            url = "https://www.cloudflare.com/",
            testTag = "credit_api_cloud"
        ),
        CreditEntry(
            title = if (isEs) "GitHub Releases y Pages" else "GitHub Releases & Pages",
            role = if (isEs) "Actualizador Integrado y Alojamiento Web" else "In-App Updater & Web App Hosting",
            description = if (isEs) {
                "Comprobación de nuevas versiones públicas, descarga directa de APK y alojamiento de LootDrop Edición Web."
            } else {
                "Public release feed checking, APK distribution, and hosting for the LootDrop Web Edition."
            },
            icon = Icons.Default.Verified,
            badge = if (isEs) "ACTUALIZ." else "UPDATES",
            url = "https://github.com/BIGBKOG/LootDrop/releases",
            testTag = "credit_api_github"
        )
    )

    CreditsGroupCard(
        entries = apiCredits,
        cardTestTag = "about_apis_card",
        isEs = isEs,
        onItemClick = { url -> openExternalUrl(context, url, isEs) }
    )

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION 4: Open-Source Libraries & License
    SectionHeader(title = s.settings.aboutTechSection, icon = Icons.Default.Layers)

    Spacer(modifier = Modifier.height(8.dp))

    val techStack = listOf(
        CreditEntry(
            title = "Kotlin & Jetpack Compose (Material 3)",
            role = if (isEs) "Lenguaje e Interfaz Declarativa" else "Language & Declarative UI",
            description = if (isEs) {
                "Componentes expresivos Material 3, barras de borde a borde, diseño adaptable y tipografía personalizada."
            } else {
                "Material 3 Expressive components, edge-to-edge system bars, adaptive layouts, and custom font theming."
            },
            icon = Icons.Default.Code,
            testTag = "credit_tech_compose"
        ),
        CreditEntry(
            title = "AndroidX Room, Lifecycle & Coroutines",
            role = if (isEs) "Almacenamiento Local y Arquitectura Reactiva" else "Local Storage & Reactive Architecture",
            description = if (isEs) {
                "Caché sin conexión, persistencia de bóveda, widgets de inicio y flujos de estado seguros para el ciclo de vida."
            } else {
                "Offline giveaway caching, vault persistence, home screen widgets, and lifecycle-safe state flows."
            },
            icon = Icons.Default.Layers,
            testTag = "credit_tech_androidx"
        ),
        CreditEntry(
            title = "Retrofit, OkHttp, Moshi & Coil",
            role = if (isEs) "Red, JSON y Carga de Portadas" else "Networking, JSON & Artwork Loading",
            description = if (isEs) {
                "Peticiones REST resistentes, caché HTTP en disco, análisis JSON en Kotlin y carga rápida de carátulas."
            } else {
                "Resilient REST API requests, disk HTTP caching, Kotlin JSON parsing, and fast cover image rendering."
            },
            icon = Icons.Default.Api,
            testTag = "credit_tech_networking"
        ),
        CreditEntry(
            title = if (isEs) "Licencia Open-Source del Proyecto" else "Open-Source Project License",
            role = if (isEs) "Licencia del Repositorio y Términos" else "Repository Licensing & Terms",
            description = if (isEs) {
                "Consulta el archivo LICENSE de LootDrop y las atribuciones de código abierto de terceros en GitHub."
            } else {
                "View the LootDrop repository LICENSE file and third-party open-source attributions on GitHub."
            },
            icon = Icons.Default.Gavel,
            badge = if (isEs) "LICENCIA" else "LICENSE",
            url = "https://github.com/BIGBKOG/LootDrop/blob/main/LICENSE",
            testTag = "credit_tech_license"
        )
    )

    CreditsGroupCard(
        entries = techStack,
        cardTestTag = "about_tech_stack_card",
        isEs = isEs,
        onItemClick = { url -> openExternalUrl(context, url, isEs) }
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedButton(
        onClick = { openExternalUrl(context, "https://www.patreon.com/cw/LootDrop", isEs) },
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
            text = if (isEs) "Apoyar a BIGBKOG y LootDrop en Patreon" else "Support BIGBKOG & LootDrop on Patreon",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CreditsGroupCard(
    entries: List<CreditEntry>,
    cardTestTag: String,
    isEs: Boolean,
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
                        contentDescription = if (isEs) "Abrir ${entry.title}" else "Open ${entry.title}",
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

private fun openExternalUrl(context: Context, url: String, isEs: Boolean = false) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(
            context,
            if (isEs) "No se pudo abrir el enlace" else "Could not open link",
            Toast.LENGTH_SHORT
        ).show()
    }
}
