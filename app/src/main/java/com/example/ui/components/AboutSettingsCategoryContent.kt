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
    val lang = s.language
    val context = LocalContext.current

    // SECTION 1: App Overview & Identity Hero Card
    SectionHeader(
        title = when (lang) {
            AppLanguage.SPANISH -> "ACERCA DE LOOTDROP"
            AppLanguage.FRENCH -> "À PROPOS DE LOOTDROP"
            AppLanguage.ENGLISH -> "ABOUT LOOTDROP"
        },
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
                        text = when (lang) {
                            AppLanguage.SPANISH -> "Compilación ${BuildConfig.VERSION_CODE} • Radar de Juegos Gratis y 100% Descuento"
                            AppLanguage.FRENCH -> "Build ${BuildConfig.VERSION_CODE} • Radar Jeux Gratuits & -100 %"
                            AppLanguage.ENGLISH -> "Build ${BuildConfig.VERSION_CODE} • Free Games & 100% Discount Radar"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = when (lang) {
                    AppLanguage.SPANISH -> "Creado para rastrear juegos 100% gratis, DLCs por tiempo limitado, biblioteca y logros de Steam, bandas sonoras y retos semanales sin anuncios ni rastreadores."
                    AppLanguage.FRENCH -> "Conçu pour suivre les jeux 100 % gratuits, DLC à durée limitée, bibliothèque et succès Steam, bandes-son et défis hebdomadaires sans pub ni traqueur."
                    AppLanguage.ENGLISH -> "Built to track 100% free games, limited-time DLC giveaways, Steam library & achievements, OSTs, and weekly backlog challenges with zero ads and zero tracking."
                },
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AboutPillBadge(
                    text = when (lang) {
                        AppLanguage.SPANISH -> "AOSP Puro"
                        AppLanguage.FRENCH -> "AOSP Pur"
                        AppLanguage.ENGLISH -> "Pure AOSP"
                    },
                    accentColor = NeonGreen
                )
                AboutPillBadge(
                    text = when (lang) {
                        AppLanguage.SPANISH -> "Cero Anuncios"
                        AppLanguage.FRENCH -> "Zéro Pub"
                        AppLanguage.ENGLISH -> "Zero Ads"
                    },
                    accentColor = MaterialTheme.colorScheme.primary
                )
                AboutPillBadge(
                    text = when (lang) {
                        AppLanguage.SPANISH -> "Código Abierto"
                        AppLanguage.FRENCH -> "Open Source"
                        AppLanguage.ENGLISH -> "Open Source"
                    },
                    accentColor = NeonCyan
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { openExternalUrl(context, "https://github.com/BIGBKOG/LootDrop", lang) },
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
                        text = when (lang) {
                            AppLanguage.SPANISH -> "Repo GitHub"
                            AppLanguage.FRENCH -> "Dépôt GitHub"
                            AppLanguage.ENGLISH -> "GitHub Repo"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = { openExternalUrl(context, "https://bigbkog.github.io/LootDrop/", lang) },
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
                        text = when (lang) {
                            AppLanguage.SPANISH -> "Edición Web"
                            AppLanguage.FRENCH -> "Édition Web"
                            AppLanguage.ENGLISH -> "Web Edition"
                        },
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
            role = when (lang) {
                AppLanguage.SPANISH -> "Creador, Desarrollador Principal y Mantenedor"
                AppLanguage.FRENCH -> "Créateur, Développeur Principal & Mainteneur"
                AppLanguage.ENGLISH -> "Creator, Lead Developer & Maintainer"
            },
            description = when (lang) {
                AppLanguage.SPANISH -> "Concepto original, app Android en Kotlin/Jetpack Compose, Edición Web en GitHub Pages, gestión de versiones y diseño de funciones."
                AppLanguage.FRENCH -> "Concept original, app Android Kotlin/Jetpack Compose, Édition Web GitHub Pages, gestion des versions et conception des fonctionnalités."
                AppLanguage.ENGLISH -> "Original concept, Kotlin/Jetpack Compose Android app, GitHub Pages Web Edition, release management & feature design."
            },
            icon = Icons.Default.Person,
            badge = when (lang) {
                AppLanguage.SPANISH -> "CREADOR"
                AppLanguage.FRENCH -> "CRÉATEUR"
                AppLanguage.ENGLISH -> "CREATOR"
            },
            url = "https://github.com/BIGBKOG",
            testTag = "credit_contributor_bigbkog"
        ),
        CreditEntry(
            title = when (lang) {
                AppLanguage.SPANISH -> "Colaboradores de la Comunidad y Patreon"
                AppLanguage.FRENCH -> "Contributeurs de la Communauté & Patreon"
                AppLanguage.ENGLISH -> "Community Contributors & Patreon Supporters"
            },
            role = when (lang) {
                AppLanguage.SPANISH -> "Testers, Reseñadores y Patrocinadores"
                AppLanguage.FRENCH -> "Testeurs, Évaluateurs & Soutiens"
                AppLanguage.ENGLISH -> "Testers, Reviewers & Backers"
            },
            description = when (lang) {
                AppLanguage.SPANISH -> "A todos los que prueban versiones, reportan errores, escriben reseñas en la comunidad y apoyan a LootDrop en Patreon."
                AppLanguage.FRENCH -> "À tous ceux qui testent les versions, signalent des bugs, publient des avis et soutiennent LootDrop sur Patreon."
                AppLanguage.ENGLISH -> "Everyone testing builds, reporting bugs, submitting community reviews, and supporting LootDrop on Patreon."
            },
            icon = Icons.Default.Favorite,
            badge = when (lang) {
                AppLanguage.SPANISH -> "COMUNIDAD"
                AppLanguage.FRENCH -> "COMMUNAUTÉ"
                AppLanguage.ENGLISH -> "COMMUNITY"
            },
            url = "https://www.patreon.com/cw/LootDrop",
            testTag = "credit_contributor_community"
        )
    )

    CreditsGroupCard(
        entries = contributors,
        cardTestTag = "about_contributors_card",
        lang = lang,
        onItemClick = { url -> openExternalUrl(context, url, lang) }
    )

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION 3: Data APIs & External Services
    SectionHeader(title = s.settings.aboutApisSection, icon = Icons.Default.Api)

    Spacer(modifier = Modifier.height(8.dp))

    val apiCredits = listOf(
        CreditEntry(
            title = "GamerPower API",
            role = when (lang) {
                AppLanguage.SPANISH -> "Feed Principal de Regalos y Botín Gratis"
                AppLanguage.FRENCH -> "Flux Principal de Jeux & Butins Gratuits"
                AppLanguage.ENGLISH -> "Primary Giveaways & Free Loot Feed"
            },
            description = when (lang) {
                AppLanguage.SPANISH -> "Rastreo en tiempo real de juegos, DLCs y recompensas 100% gratis en PC, Steam, Epic Games, GOG, PlayStation, Xbox y móvil."
                AppLanguage.FRENCH -> "Suivi en temps réel des jeux, DLC et récompenses 100 % gratuits sur PC, Steam, Epic Games, GOG, PlayStation, Xbox et mobile."
                AppLanguage.ENGLISH -> "Real-time 100% free game, DLC, and loot drop tracking across PC, Steam, Epic Games, GOG, PlayStation, Xbox & mobile."
            },
            icon = Icons.Default.SportsEsports,
            badge = when (lang) {
                AppLanguage.SPANISH -> "API PRINCIPAL"
                AppLanguage.FRENCH -> "API PRINCIPALE"
                AppLanguage.ENGLISH -> "CORE API"
            },
            url = "https://www.gamerpower.com/api-read",
            testTag = "credit_api_gamerpower"
        ),
        CreditEntry(
            title = "CheapShark API",
            role = when (lang) {
                AppLanguage.SPANISH -> "Ofertas de Tiendas y Comparación de Precios"
                AppLanguage.FRENCH -> "Offres des Boutiques & Comparaison de Prix"
                AppLanguage.ENGLISH -> "Storefront Deals & Price Comparisons"
            },
            description = when (lang) {
                AppLanguage.SPANISH -> "Búsqueda de precios entre tiendas, comparativa de ahorro en tienda y seguimiento de ofertas en tiendas digitales de PC."
                AppLanguage.FRENCH -> "Recherche de prix multi-boutiques, comparaison d'économies et suivi des promotions PC."
                AppLanguage.ENGLISH -> "Cross-store price lookups, retail savings comparisons, and deal tracking across major digital PC storefronts."
            },
            icon = Icons.Default.LocalOffer,
            badge = when (lang) {
                AppLanguage.SPANISH -> "API OFERTAS"
                AppLanguage.FRENCH -> "API OFFRES"
                AppLanguage.ENGLISH -> "DEALS API"
            },
            url = "https://apidocs.cheapshark.com/",
            testTag = "credit_api_cheapshark"
        ),
        CreditEntry(
            title = when (lang) {
                AppLanguage.SPANISH -> "API Web y Tienda de Steam"
                AppLanguage.FRENCH -> "API Web & Boutique Steam"
                AppLanguage.ENGLISH -> "Steam Web & Storefront API"
            },
            role = when (lang) {
                AppLanguage.SPANISH -> "Sincronización de Biblioteca, Logros y Radar DLC"
                AppLanguage.FRENCH -> "Synchronisation Bibliothèque, Succès & Radar DLC"
                AppLanguage.ENGLISH -> "Library Sync, Achievements & DLC Radar"
            },
            description = when (lang) {
                AppLanguage.SPANISH -> "Consulta de juegos de Steam, porcentajes globales y personales de logros, lista de deseados y recomendaciones de DLC."
                AppLanguage.FRENCH -> "Jeux Steam possédés, taux de succès globaux et personnels, listes de souhaits et recommandations DLC."
                AppLanguage.ENGLISH -> "Owned Steam games lookup, global & personal achievement percentages, wishlists, and DLC recommendations."
            },
            icon = Icons.Default.Storefront,
            badge = "STEAM",
            url = "https://store.steampowered.com/",
            testTag = "credit_api_steam"
        ),
        CreditEntry(
            title = when (lang) {
                AppLanguage.SPANISH -> "APIs de MusicBrainz, Discogs y Spotify"
                AppLanguage.FRENCH -> "APIs MusicBrainz, Discogs & Spotify"
                AppLanguage.ENGLISH -> "MusicBrainz, Discogs & Spotify APIs"
            },
            role = when (lang) {
                AppLanguage.SPANISH -> "Descubrimiento de Bandas Sonoras (OST)"
                AppLanguage.FRENCH -> "Découverte de Bandes Originales (OST)"
                AppLanguage.ENGLISH -> "Official Soundtrack (OST) Discovery"
            },
            description = when (lang) {
                AppLanguage.SPANISH -> "Metadatos de álbumes, listas de pistas, años de lanzamiento y enlaces de reproducción de OST para tu colección."
                AppLanguage.FRENCH -> "Métadonnées d'albums, listes de pistes, années de sortie et liens d'écoute OST pour votre collection."
                AppLanguage.ENGLISH -> "Album metadata, tracklists, release years, and soundtrack streaming links for games in your collection."
            },
            icon = Icons.Default.Album,
            badge = when (lang) {
                AppLanguage.SPANISH -> "DATOS OST"
                AppLanguage.FRENCH -> "DONNÉES OST"
                AppLanguage.ENGLISH -> "OST DATA"
            },
            url = "https://musicbrainz.org/",
            testTag = "credit_api_ost"
        ),
        CreditEntry(
            title = when (lang) {
                AppLanguage.SPANISH -> "Cloudflare Workers / R2 y Supabase"
                AppLanguage.FRENCH -> "Cloudflare Workers / R2 & Supabase"
                AppLanguage.ENGLISH -> "Cloudflare Workers / R2 & Supabase"
            },
            role = when (lang) {
                AppLanguage.SPANISH -> "Reseñas Públicas y Respaldo en la Nube"
                AppLanguage.FRENCH -> "Avis Publics & Sauvegarde Cloud"
                AppLanguage.ENGLISH -> "Community Reviews & Optional Vault Sync"
            },
            description = when (lang) {
                AppLanguage.SPANISH -> "Almacenamiento de reseñas de la comunidad y sincronización opcional de respaldo de cuenta entre dispositivos."
                AppLanguage.FRENCH -> "Stockage des avis de la communauté et synchronisation cloud optionnelle du coffre entre appareils."
                AppLanguage.ENGLISH -> "Public community reviews storage and optional encrypted account backup sync across devices."
            },
            icon = Icons.Default.CloudQueue,
            badge = when (lang) {
                AppLanguage.SPANISH -> "NUBE"
                AppLanguage.FRENCH -> "CLOUD"
                AppLanguage.ENGLISH -> "CLOUD"
            },
            url = "https://www.cloudflare.com/",
            testTag = "credit_api_cloud"
        ),
        CreditEntry(
            title = when (lang) {
                AppLanguage.SPANISH -> "GitHub Releases y Pages"
                AppLanguage.FRENCH -> "GitHub Releases & Pages"
                AppLanguage.ENGLISH -> "GitHub Releases & Pages"
            },
            role = when (lang) {
                AppLanguage.SPANISH -> "Actualizador Integrado y Alojamiento Web"
                AppLanguage.FRENCH -> "Mise à Jour Intégrée & Hébergement Web"
                AppLanguage.ENGLISH -> "In-App Updater & Web App Hosting"
            },
            description = when (lang) {
                AppLanguage.SPANISH -> "Comprobación de nuevas versiones públicas, descarga directa de APK y alojamiento de LootDrop Edición Web."
                AppLanguage.FRENCH -> "Vérification des versions publiques, téléchargement direct d'APK et hébergement de LootDrop Édition Web."
                AppLanguage.ENGLISH -> "Public release feed checking, APK distribution, and hosting for the LootDrop Web Edition."
            },
            icon = Icons.Default.Verified,
            badge = when (lang) {
                AppLanguage.SPANISH -> "ACTUALIZ."
                AppLanguage.FRENCH -> "MAJ"
                AppLanguage.ENGLISH -> "UPDATES"
            },
            url = "https://github.com/BIGBKOG/LootDrop/releases",
            testTag = "credit_api_github"
        )
    )

    CreditsGroupCard(
        entries = apiCredits,
        cardTestTag = "about_apis_card",
        lang = lang,
        onItemClick = { url -> openExternalUrl(context, url, lang) }
    )

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION 4: Open-Source Libraries & License
    SectionHeader(title = s.settings.aboutTechSection, icon = Icons.Default.Layers)

    Spacer(modifier = Modifier.height(8.dp))

    val techStack = listOf(
        CreditEntry(
            title = "Kotlin & Jetpack Compose (Material 3)",
            role = when (lang) {
                AppLanguage.SPANISH -> "Lenguaje e Interfaz Declarativa"
                AppLanguage.FRENCH -> "Langage & Interface Déclarative"
                AppLanguage.ENGLISH -> "Language & Declarative UI"
            },
            description = when (lang) {
                AppLanguage.SPANISH -> "Componentes expresivos Material 3, barras de borde a borde, diseño adaptable y tipografía personalizada."
                AppLanguage.FRENCH -> "Composants expressifs Material 3, barres bord à bord, mise en page adaptative et polices personnalisées."
                AppLanguage.ENGLISH -> "Material 3 Expressive components, edge-to-edge system bars, adaptive layouts, and custom font theming."
            },
            icon = Icons.Default.Code,
            testTag = "credit_tech_compose"
        ),
        CreditEntry(
            title = "AndroidX Room, Lifecycle & Coroutines",
            role = when (lang) {
                AppLanguage.SPANISH -> "Almacenamiento Local y Arquitectura Reactiva"
                AppLanguage.FRENCH -> "Stockage Local & Architecture Réactive"
                AppLanguage.ENGLISH -> "Local Storage & Reactive Architecture"
            },
            description = when (lang) {
                AppLanguage.SPANISH -> "Caché sin conexión, persistencia de bóveda, widgets de inicio y flujos de estado seguros para el ciclo de vida."
                AppLanguage.FRENCH -> "Cache hors ligne, persistance du coffre, widgets d'accueil et flux d'état sûrs."
                AppLanguage.ENGLISH -> "Offline giveaway caching, vault persistence, home screen widgets, and lifecycle-safe state flows."
            },
            icon = Icons.Default.Layers,
            testTag = "credit_tech_androidx"
        ),
        CreditEntry(
            title = "Retrofit, OkHttp, Moshi & Coil",
            role = when (lang) {
                AppLanguage.SPANISH -> "Red, JSON y Carga de Portadas"
                AppLanguage.FRENCH -> "Réseau, JSON & Chargement d'Images"
                AppLanguage.ENGLISH -> "Networking, JSON & Artwork Loading"
            },
            description = when (lang) {
                AppLanguage.SPANISH -> "Peticiones REST resistentes, caché HTTP en disco, análisis JSON en Kotlin y carga rápida de carátulas."
                AppLanguage.FRENCH -> "Requêtes REST résilientes, cache HTTP sur disque, parsing JSON Kotlin et rendu rapide des jaquettes."
                AppLanguage.ENGLISH -> "Resilient REST API requests, disk HTTP caching, Kotlin JSON parsing, and fast cover image rendering."
            },
            icon = Icons.Default.Api,
            testTag = "credit_tech_networking"
        ),
        CreditEntry(
            title = when (lang) {
                AppLanguage.SPANISH -> "Licencia Open-Source del Proyecto"
                AppLanguage.FRENCH -> "Licence Open-Source du Projet"
                AppLanguage.ENGLISH -> "Open-Source Project License"
            },
            role = when (lang) {
                AppLanguage.SPANISH -> "Licencia del Repositorio y Términos"
                AppLanguage.FRENCH -> "Licence du Dépôt & Conditions"
                AppLanguage.ENGLISH -> "Repository Licensing & Terms"
            },
            description = when (lang) {
                AppLanguage.SPANISH -> "Consulta el archivo LICENSE de LootDrop y las atribuciones de código abierto de terceros en GitHub."
                AppLanguage.FRENCH -> "Consultez le fichier LICENSE de LootDrop et les attributions open-source tierces sur GitHub."
                AppLanguage.ENGLISH -> "View the LootDrop repository LICENSE file and third-party open-source attributions on GitHub."
            },
            icon = Icons.Default.Gavel,
            badge = when (lang) {
                AppLanguage.SPANISH -> "LICENCIA"
                AppLanguage.FRENCH -> "LICENCE"
                AppLanguage.ENGLISH -> "LICENSE"
            },
            url = "https://github.com/BIGBKOG/LootDrop/blob/main/LICENSE",
            testTag = "credit_tech_license"
        )
    )

    CreditsGroupCard(
        entries = techStack,
        cardTestTag = "about_tech_stack_card",
        lang = lang,
        onItemClick = { url -> openExternalUrl(context, url, lang) }
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedButton(
        onClick = { openExternalUrl(context, "https://www.patreon.com/cw/LootDrop", lang) },
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
            text = when (lang) {
                AppLanguage.SPANISH -> "Apoyar a BIGBKOG y LootDrop en Patreon"
                AppLanguage.FRENCH -> "Soutenir BIGBKOG & LootDrop sur Patreon"
                AppLanguage.ENGLISH -> "Support BIGBKOG & LootDrop on Patreon"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CreditsGroupCard(
    entries: List<CreditEntry>,
    cardTestTag: String,
    lang: AppLanguage,
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
                        contentDescription = when (lang) {
                            AppLanguage.SPANISH -> "Abrir ${entry.title}"
                            AppLanguage.FRENCH -> "Ouvrir ${entry.title}"
                            AppLanguage.ENGLISH -> "Open ${entry.title}"
                        },
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

private fun openExternalUrl(context: Context, url: String, lang: AppLanguage = AppLanguage.ENGLISH) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(
            context,
            when (lang) {
                AppLanguage.SPANISH -> "No se pudo abrir el enlace"
                AppLanguage.FRENCH -> "Impossible d'ouvrir le lien"
                AppLanguage.ENGLISH -> "Could not open link"
            },
            Toast.LENGTH_SHORT
        ).show()
    }
}
