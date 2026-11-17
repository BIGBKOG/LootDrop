package com.example.i18n

import com.example.data.model.BacklogChallengeCategory
import com.example.data.model.BacklogChallengeTile
import com.example.data.model.BacklogMilestoneReward
import com.example.data.model.ControllerSupport
import com.example.data.model.GameCategory
import com.example.data.model.LauncherType
import com.example.data.model.LibraryPlayFilter
import com.example.data.model.LibraryViewMode
import com.example.data.model.MetacriticRatingTier
import com.example.data.model.PlayStatus
import com.example.data.model.PlayabilityFilter
import com.example.data.model.SessionStyle
import com.example.data.model.SteamDeckStatus
import com.example.data.model.StorageFilter
import com.example.data.model.StorageTier
import com.example.data.model.TrophyRarityTier
import com.example.util.BacklogChallengeManager
import com.example.preferences.AccentCategory
import com.example.preferences.AccentTheme
import com.example.preferences.BackgroundTheme
import com.example.preferences.CardDensity
import com.example.preferences.DpiScaleMode
import com.example.preferences.TargetFrameRate
import com.example.preferences.ThemeMode
import com.example.ui.MainTab
import com.example.ui.SettingsCategory
import com.example.ui.SortOption
import com.example.ui.TypeFilter
import com.example.ui.calendar.CalendarFilterScope

fun MainTab.localizedLabel(s: AppStrings): String = when (this) {
    MainTab.HOME -> s.common.tabHome
    MainTab.DISCOVER -> s.common.tabDiscover
    MainTab.CLAIMED -> s.common.tabClaimed
    MainTab.CALENDAR -> s.common.tabCalendar
    MainTab.SETTINGS -> s.common.tabSettings
}

fun SortOption.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        SortOption.NEWEST -> "Más recientes"
        SortOption.HIGHEST_METACRITIC -> "Metascore (Mejor nota)"
        SortOption.HIGHEST_VALUE -> "Mayor valor ($)"
        SortOption.MOST_POPULAR -> "Más reclamados"
        SortOption.ENDING_SOON -> "Terminan pronto"
    }

fun TypeFilter.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        TypeFilter.ALL -> "Todo"
        TypeFilter.FULL_GAMES -> "Juegos"
        TypeFilter.DLC_LOOT -> "DLC y Botín"
        TypeFilter.EARLY_ACCESS -> "Beta / Acceso"
    }

fun SettingsCategory.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        SettingsCategory.APPEARANCE -> "Apariencia"
        SettingsCategory.ALERTS -> "Alertas"
        SettingsCategory.GENERAL -> "General"
        SettingsCategory.STORAGE -> "Almacenamiento"
        SettingsCategory.ABOUT -> "Acerca de"
    }

fun SettingsCategory.localizedDescription(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) description else when (this) {
        SettingsCategory.APPEARANCE -> "Tema, colores de acento, fondo y densidad de tarjetas"
        SettingsCategory.ALERTS -> "Centro de notificaciones, sonido, vibración y reglas de tiendas"
        SettingsCategory.GENERAL -> "Idioma, tienda inicial, rendimiento, actualizaciones y guía"
        SettingsCategory.STORAGE -> "Métricas de uso, limpieza rápida, respaldos JSON y nube"
        SettingsCategory.ABOUT -> "Créditos del creador, APIs usadas, licencias y soporte"
    }

fun ThemeMode.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        ThemeMode.DARK -> "Modo Oscuro"
        ThemeMode.LIGHT -> "Modo Claro"
        ThemeMode.SYSTEM -> "Según Sistema"
    }

fun CardDensity.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        CardDensity.RICH_CARDS -> "Tarjetas Ricas"
        CardDensity.COMPACT_CARDS -> "Tarjetas Compactas"
        CardDensity.MINIMAL_LIST -> "Lista Mínima"
    }

fun CardDensity.localizedDescription(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) description else when (this) {
        CardDensity.RICH_CARDS -> "Portada grande con todos los detalles"
        CardDensity.COMPACT_CARDS -> "Tarjetas horizontales equilibradas"
        CardDensity.MINIMAL_LIST -> "Vista compacta priorizando el texto"
    }

fun TargetFrameRate.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        TargetFrameRate.AUTO -> "Adaptativo (60/90/120)"
        TargetFrameRate.FPS_60 -> "60 FPS"
        TargetFrameRate.FPS_90 -> "90 FPS"
        TargetFrameRate.FPS_120 -> "120 FPS"
    }

fun TargetFrameRate.localizedDescription(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) description else when (this) {
        TargetFrameRate.AUTO -> "Sincronización adaptativa hasta 120Hz"
        TargetFrameRate.FPS_60 -> "Frecuencia estándar de 60Hz"
        TargetFrameRate.FPS_90 -> "Movimiento fluido a 90Hz"
        TargetFrameRate.FPS_120 -> "Máxima fluidez extrema a 120Hz"
    }

fun DpiScaleMode.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        DpiScaleMode.AUTO_ADAPTIVE -> "Auto (Según DPI)"
        DpiScaleMode.COMPACT -> "Compacto (Alta Densidad)"
        DpiScaleMode.BALANCED -> "Estándar Equilibrado"
        DpiScaleMode.SPACIOUS -> "Amplio y Cómodo"
    }

fun DpiScaleMode.localizedShortName(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) shortName else when (this) {
        DpiScaleMode.AUTO_ADAPTIVE -> "Auto"
        DpiScaleMode.COMPACT -> "Compacto"
        DpiScaleMode.BALANCED -> "Normal"
        DpiScaleMode.SPACIOUS -> "Amplio"
    }

fun DpiScaleMode.localizedDescription(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) description else when (this) {
        DpiScaleMode.AUTO_ADAPTIVE -> "Ajusta diseño, espacios y botones al DPI de pantalla"
        DpiScaleMode.COMPACT -> "Espaciado compacto para ver más juegos en pantalla"
        DpiScaleMode.BALANCED -> "Espaciado y tamaños estándar de Material 3"
        DpiScaleMode.SPACIOUS -> "Botones más grandes y márgenes generosos"
    }

fun AccentCategory.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        AccentCategory.ALL -> "Todos (19)"
        AccentCategory.GAMING -> "Gaming (6)"
        AccentCategory.CYBER -> "Cyber (5)"
        AccentCategory.VAULT -> "Bóveda (5)"
        AccentCategory.REWARDS -> "Premios (3)"
    }

fun AccentTheme.localizedTitle(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) title else when (this) {
        AccentTheme.CYBER_CYAN -> "Cian Cyber"
        AccentTheme.STEAM_BLUE -> "Azul Steam"
        AccentTheme.GOG_PURPLE -> "Ultravioleta"
        AccentTheme.RAZER_LIME -> "Lima Tóxico"
        AccentTheme.INFERNO_RED -> "Rojo Infierno"
        AccentTheme.GOLDEN_LOOT -> "Botín Dorado"
        AccentTheme.EPIC_INDIGO -> "Índigo Epic"
        AccentTheme.CHERRY_ROSE -> "Flor de Cerezo"
        AccentTheme.OCEAN_TEAL -> "Turquesa Océano"
        AccentTheme.SUNSET_CORAL -> "Coral Ocaso"
        AccentTheme.DEEP_ROYALE -> "Violeta Real"
        AccentTheme.EMERALD_MINT -> "Esmeralda"
        AccentTheme.PLAYSTATION_BLUE -> "Azul PlayStation"
        AccentTheme.XBOX_GREEN -> "Verde Xbox"
        AccentTheme.NINTENDO_RED -> "Rojo Nintendo"
        AccentTheme.DISCORD_BLURPLE -> "Azul Discord"
        AccentTheme.BACKLOG_SLAYER -> "Matabacklog"
        AccentTheme.VAULT_CLEANER -> "Oro de Bóveda"
        AccentTheme.NEBULA_BLACKOUT -> "Nebulosa Oscura"
    }

fun AccentTheme.localizedDescription(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) description else when (this) {
        AccentTheme.CYBER_CYAN -> "Azul cibernético vibrante de alto contraste"
        AccentTheme.STEAM_BLUE -> "Azul clásico de gaming en PC"
        AccentTheme.GOG_PURPLE -> "Violeta eléctrico intenso y magenta"
        AccentTheme.RAZER_LIME -> "Verde matrix lleno de energía"
        AccentTheme.INFERNO_RED -> "Rojo combate y brasa ardiente"
        AccentTheme.GOLDEN_LOOT -> "Dorado legendario de recompensas"
        AccentTheme.EPIC_INDIGO -> "Índigo profundo de launcher"
        AccentTheme.CHERRY_ROSE -> "Rosa neón vivo y sakura"
        AccentTheme.OCEAN_TEAL -> "Turquesa cristalino oceánico"
        AccentTheme.SUNSET_CORAL -> "Destello cálido de atardecer"
        AccentTheme.DEEP_ROYALE -> "Púrpura imperial majestuoso"
        AccentTheme.EMERALD_MINT -> "Verde gema esmeralda brillante"
        AccentTheme.PLAYSTATION_BLUE -> "Cobalto eléctrico de PlayStation"
        AccentTheme.XBOX_GREEN -> "Verde neón icónico de Xbox"
        AccentTheme.NINTENDO_RED -> "Carmesí neón icónico de Nintendo"
        AccentTheme.DISCORD_BLURPLE -> "Tono clásico de comunidad gamer"
        AccentTheme.BACKLOG_SLAYER -> "¡Desbloqueado en Reto Backlog! Carmesí neón"
        AccentTheme.VAULT_CLEANER -> "¡Desbloqueado en Reto Backlog! Oro puro 24K"
        AccentTheme.NEBULA_BLACKOUT -> "¡Desbloqueado en Reto Backlog! Violeta cósmico"
    }

fun AccentTheme.localizedShortName(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) shortName else when (this) {
        AccentTheme.CYBER_CYAN -> "Cian"
        AccentTheme.STEAM_BLUE -> "Steam"
        AccentTheme.GOG_PURPLE -> "Ultravioleta"
        AccentTheme.RAZER_LIME -> "Lima"
        AccentTheme.INFERNO_RED -> "Infierno"
        AccentTheme.GOLDEN_LOOT -> "Oro"
        AccentTheme.EPIC_INDIGO -> "Epic"
        AccentTheme.CHERRY_ROSE -> "Sakura"
        AccentTheme.OCEAN_TEAL -> "Turquesa"
        AccentTheme.SUNSET_CORAL -> "Ocaso"
        AccentTheme.DEEP_ROYALE -> "Real"
        AccentTheme.EMERALD_MINT -> "Esmeralda"
        AccentTheme.PLAYSTATION_BLUE -> "PlayStation"
        AccentTheme.XBOX_GREEN -> "Xbox"
        AccentTheme.NINTENDO_RED -> "Nintendo"
        AccentTheme.DISCORD_BLURPLE -> "Discord"
        AccentTheme.BACKLOG_SLAYER -> "Slayer"
        AccentTheme.VAULT_CLEANER -> "Bóveda"
        AccentTheme.NEBULA_BLACKOUT -> "Nebulosa"
    }

fun BackgroundTheme.localizedTitle(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) title else when (this) {
        BackgroundTheme.CYBER_SLATE -> "Pizarra Cyber"
        BackgroundTheme.OLED_BLACK -> "Negro OLED"
        BackgroundTheme.MIDNIGHT_NAVY -> "Azul Medianoche"
        BackgroundTheme.CLEAN_SNOW -> "Nieve Pura"
        BackgroundTheme.COOL_FROST -> "Escarcha Fría"
        BackgroundTheme.WARM_SAND -> "Arena Cálida"
    }

fun LauncherType.localizedDisplayName(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) displayName else when (this) {
        LauncherType.ALL -> "Todas las Plataformas"
        LauncherType.OTHER -> "DRM-Free / Otros"
        else -> displayName
    }

fun GameCategory.localizedDisplayName(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) displayName else when (this) {
        GameCategory.FULL_GAME -> "Juego Completo"
        GameCategory.DLC_LOOT -> "DLC y Packs"
        GameCategory.ACTION -> "Acción"
        GameCategory.RPG -> "RPG"
        GameCategory.SHOOTER -> "Disparos"
        GameCategory.STRATEGY -> "Estrategia"
        GameCategory.ADVENTURE -> "Aventura"
        GameCategory.INDIE -> "Indie"
        GameCategory.SIMULATION -> "Simulación"
        GameCategory.HORROR -> "Terror"
        GameCategory.MULTIPLAYER -> "Multijugador"
        GameCategory.RACING_SPORTS -> "Carreras y Deportes"
        GameCategory.PUZZLE -> "Puzle y Casual"
        GameCategory.SCI_FI -> "Ciencia Ficción"
        GameCategory.FANTASY -> "Fantasía"
    }

fun GameCategory.localizedDescription(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) description else when (this) {
        GameCategory.FULL_GAME -> "Juegos base completos (100% gratis)"
        GameCategory.DLC_LOOT -> "Expansiones, skins, extras y paquetes"
        GameCategory.ACTION -> "Combate rápido, acción y peleas"
        GameCategory.RPG -> "Rol, progresión de personaje e historia"
        GameCategory.SHOOTER -> "FPS, tercera persona y acción táctica"
        GameCategory.STRATEGY -> "Estrategia en tiempo real, turnos y táctica"
        GameCategory.ADVENTURE -> "Narrativa, exploración e historias épicas"
        GameCategory.INDIE -> "Títulos independientes, pixel art y roguelike"
        GameCategory.SIMULATION -> "Simuladores, gestión, construcción y sandbox"
        GameCategory.HORROR -> "Supervivencia, atmósfera oscura y suspenso"
        GameCategory.MULTIPLAYER -> "Cooperativo online, PvP, MMO y equipos"
        GameCategory.RACING_SPORTS -> "Conducción, automovilismo y deportes"
        GameCategory.PUZZLE -> "Lógica, ingenio, cartas y juegos casuales"
        GameCategory.SCI_FI -> "Futurista, espacio, alienígenas y cyberpunk"
        GameCategory.FANTASY -> "Magia, mundos medievales, dragones y reinos"
    }

fun PlayStatus.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        PlayStatus.NOT_STARTED -> "Pendiente"
        PlayStatus.PLAYING -> "Jugando"
        PlayStatus.BEATEN -> "Terminado"
        PlayStatus.DROPPED -> "Abandonado"
    }

fun LibraryPlayFilter.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        LibraryPlayFilter.ALL -> "Todos"
        LibraryPlayFilter.UNPLAYED -> "Sin jugar"
        LibraryPlayFilter.PLAYING -> "Jugando"
        LibraryPlayFilter.FINISHED -> "Terminados"
        LibraryPlayFilter.DROPPED -> "Abandonados"
    }

fun LibraryViewMode.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        LibraryViewMode.VAULT -> "Bóveda"
        LibraryViewMode.ALL_PAID -> "Todos + Pago"
        LibraryViewMode.ACHIEVEMENTS -> "Trofeos"
    }

fun PlayabilityFilter.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        PlayabilityFilter.ALL -> "Todo"
        PlayabilityFilter.DECK_VERIFIED -> "Verificado Deck"
        PlayabilityFilter.CONTROLLER_FRIENDLY -> "Compatible Mando"
        PlayabilityFilter.QUICK_SESSION -> "Sesión Rápida"
    }

fun StorageFilter.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        StorageFilter.ALL -> "Todos los Tamaños"
        else -> label
    }

fun SteamDeckStatus.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        SteamDeckStatus.VERIFIED -> "Verificado Deck"
        SteamDeckStatus.PLAYABLE -> "Jugable en Deck"
        SteamDeckStatus.UNSUPPORTED -> "No Compatible"
        SteamDeckStatus.UNKNOWN -> "Sin Probar"
    }

fun SteamDeckStatus.localizedDescription(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) description else when (this) {
        SteamDeckStatus.VERIFIED -> "Verificado por Valve: Controles listos, texto legible, 1280x800 y 60+ FPS fluidos en Steam Deck."
        SteamDeckStatus.PLAYABLE -> "Jugable en Steam Deck: Puede requerir teclado táctil o ajustes menores de mando."
        SteamDeckStatus.UNSUPPORTED -> "No compatible con Steam Deck por anti-cheat de kernel o dependencias exclusivas de Windows."
        SteamDeckStatus.UNKNOWN -> "Pendiente de revisión oficial de compatibilidad por Valve."
    }

fun SteamDeckStatus.localizedPillLabel(s: AppStrings): String = "$iconEmoji ${localizedLabel(s)}"

fun ControllerSupport.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        ControllerSupport.FULL -> "Mando Total"
        ControllerSupport.PARTIAL -> "Mando Parcial"
        ControllerSupport.KEYBOARD_MOUSE -> "Teclado y Ratón"
    }

fun ControllerSupport.localizedDescription(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) description else when (this) {
        ControllerSupport.FULL -> "Soporte nativo completo para mandos de Xbox, PlayStation y Steam Deck."
        ControllerSupport.PARTIAL -> "Compatible con mando al jugar, pero puede requerir ratón o táctil en el menú inicial."
        ControllerSupport.KEYBOARD_MOUSE -> "Diseñado principalmente para teclado y ratón."
    }

fun SessionStyle.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        SessionStyle.QUICK_SESSION -> "Sesión Rápida"
        SessionStyle.DEEP_DIVE -> "Partida Larga"
    }

fun SessionStyle.localizedDescription(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) description else when (this) {
        SessionStyle.QUICK_SESSION -> "Partidas rápidas (< 30 min), ideal para portátil y ratos libres."
        SessionStyle.DEEP_DIVE -> "Experiencia profunda (45+ min): RPG narrativos, mundos abiertos o estrategia."
    }

fun StorageTier.localizedDescription(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) description else when (this) {
        StorageTier.FEATHERWEIGHT -> "Ligero (< 2 GB): Amigable para microSD, descarga en segundos."
        StorageTier.COMPACT -> "Compacto (2 - 10 GB): Espacio moderado, descarga veloz."
        StorageTier.MEDIUM -> "Medio (10 - 40 GB): Tamaño estándar de juego PC AA/AAA."
        StorageTier.HEAVYWEIGHT -> "Pesado (40+ GB): Superproducción AAA, reserva espacio suficiente."
    }

fun CalendarFilterScope.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        CalendarFilterScope.NEXT_14_DAYS -> "Próx. 14 días"
        CalendarFilterScope.NEXT_7_DAYS -> "Próx. 7 días"
        CalendarFilterScope.URGENT_48H -> "Urgente (≤ 48h)"
    }

fun TrophyRarityTier.localizedLabel(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) label else when (this) {
        TrophyRarityTier.MYTHIC -> "Trofeo Mítico"
        TrophyRarityTier.GOLD -> "Trofeo de Oro"
        TrophyRarityTier.SILVER -> "Trofeo de Plata"
        TrophyRarityTier.BRONZE -> "Trofeo de Bronce"
        TrophyRarityTier.COMMON -> "Estándar"
    }

fun TrophyRarityTier.localizedDescription(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) description else when (this) {
        TrophyRarityTier.MYTHIC -> "Ultra raro: Desbloqueado por menos del 5% de jugadores."
        TrophyRarityTier.GOLD -> "Raro: Desbloqueado por 5% a 15% de jugadores."
        TrophyRarityTier.SILVER -> "Poco común: Desbloqueado por 15% a 35% de jugadores."
        TrophyRarityTier.BRONZE -> "Común: Desbloqueado por 35% a 65% de jugadores."
        TrophyRarityTier.COMMON -> "Historia / Base: Más del 65% de jugadores."
    }

fun BacklogChallengeCategory.localizedDisplayName(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) displayName else when (this) {
        BacklogChallengeCategory.TROPHY -> "Cazatrofeos"
        BacklogChallengeCategory.COMPLETION -> "Finalización"
        BacklogChallengeCategory.GENRE -> "Especialista"
        BacklogChallengeCategory.PLAYTIME -> "Inmersión"
        BacklogChallengeCategory.EXPLORATION -> "Exploración"
    }

fun BacklogChallengeTile.localizedTitle(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) title else when (challengeType) {
        "GOLD_TROPHY" -> "Logra un Trofeo de Oro"
        "INDIE_GAME" -> "Juega un Título Indie"
        "IMMERSION_SESSION", "TWO_HOUR_IMMERSION" -> "Inmersión de 1 Hora"
        "VAULT_STARTER" -> "Estreno de Bóveda"
        "BEAT_GAME" -> "Termina un Juego"
        "CONTROLLER_READY" -> "Listo para Mando"
        "TROPHY_COLLECTOR" -> "Coleccionista de Trofeos"
        "MYTHIC_HUNTER" -> "Cazador Mítico"
        "TRI_PLAY" -> "Doble Sesión Backlog"
        "METACRITIC_80" -> "Aclamado por Crítica"
        "DECK_VERIFIED" -> "Héroe Steam Deck"
        "QUICK_SESSION" -> "Sprint Corto"
        "UNPLAYED_REVIVAL" -> "Nuevo Comienzo"
        "FIVE_TROPHIES", "TEN_TROPHIES" -> "Sprint de Trofeos"
        "COMPLETION_JOURNEY" -> "Finalizador Semanal"
        "COMPACT_STORAGE" -> "Joya Ligera"
        "HEAVYWEIGHT_EPIC" -> "Inmersión Triple-A"
        "GENRE_HOPPER" -> "Explorador de Géneros"
        "DROPPED_REVIVAL" -> "Segunda Oportunidad"
        "CLOSE_TO_HUNDRED" -> "Empujón al 100%"
        else -> title
    }

fun BacklogChallengeTile.localizedDescription(s: AppStrings): String =
    if (s.language == AppLanguage.ENGLISH) description else when (challengeType) {
        "GOLD_TROPHY" -> "Obtén cualquier trofeo de Steam nivel Oro o Mítico (<15% de desbloqueo global)."
        "INDIE_GAME" -> "Juega o avanza un título indie de tu colección por 20+ min o termínalo."
        "IMMERSION_SESSION", "TWO_HOUR_IMMERSION" -> "Juega un título pendiente o clásico de tu backlog por 60+ minutos."
        "VAULT_STARTER" -> "Inicia o juega un título gratis reclamado en tu Bóveda de LootDrop."
        "BEAT_GAME" -> "¡Termina cualquier juego del backlog y márcalo como Terminado en tu biblioteca!"
        "CONTROLLER_READY" -> "Juega cualquier título con soporte verificado de mando por 25+ min."
        "TROPHY_COLLECTOR" -> "Desbloquea 3 trofeos en total en cualquier juego de tu colección."
        "MYTHIC_HUNTER" -> "Desbloquea un trofeo Mítico ultra raro (<5% de desbloqueo global)."
        "TRI_PLAY" -> "Juega o registra tiempo en 2 juegos distintos de tu biblioteca esta semana."
        "METACRITIC_80" -> "Juega un título con Metacritic 80+ durante al menos 30 minutos."
        "DECK_VERIFIED" -> "Juega cualquier título Verificado para Steam Deck de tu colección."
        "QUICK_SESSION" -> "Juega un título de sesiones rápidas (<30 min) durante 15+ minutos."
        "UNPLAYED_REVIVAL" -> "Abre un juego pendiente sin jugar y registra 30+ minutos de partida."
        "FIVE_TROPHIES", "TEN_TROPHIES" -> "Desbloquea 5 trofeos en total en toda tu biblioteca gamer."
        "COMPLETION_JOURNEY" -> "Ten 1 o más juegos de tu backlog marcados como Terminados."
        "COMPACT_STORAGE" -> "Juega cualquier título de menos de 2 GB de peso durante 20+ minutos."
        "HEAVYWEIGHT_EPIC" -> "Juega un título grande de 10 GB+ de tu biblioteca al menos 45 minutos."
        "GENRE_HOPPER" -> "Juega títulos de 2 géneros distintos durante esta semana."
        "DROPPED_REVIVAL" -> "Retoma un juego pausado o abandonado y pásalo a Jugando o Terminado."
        "CLOSE_TO_HUNDRED" -> "Juega o desbloquea logros en un título con 75%+ completado."
        else -> description
    }

fun BacklogMilestoneReward.localizedTitle(s: AppStrings): String {
    if (s.language == AppLanguage.ENGLISH) return title
    return when {
        id.startsWith(BacklogChallengeManager.MILESTONE_BACKLOG_SLAYER) -> "Trofeo y Tema Matabacklog"
        id.startsWith(BacklogChallengeManager.MILESTONE_ROW_RAIDER) -> "Trofeo Asaltante de Filas"
        id.startsWith(BacklogChallengeManager.MILESTONE_COLUMN_CONQUEROR) -> "Trofeo Conquistador de Columnas"
        id.startsWith(BacklogChallengeManager.MILESTONE_VAULT_CLEANER) -> "Trofeo y Tema Oro de Bóveda 24K"
        id.startsWith(BacklogChallengeManager.MILESTONE_BLACKOUT_CHAMPION) -> "Tema Nebulosa y Trofeo Blackout"
        else -> title
    }
}

fun MetacriticRatingTier.localizedLabel(s: AppStrings): String = when (this) {
    MetacriticRatingTier.UNIVERSAL_ACCLAIM -> s.library.metacriticUniversalAcclaim
    MetacriticRatingTier.GENERALLY_FAVORABLE -> s.library.metacriticGenerallyFavorable
    MetacriticRatingTier.MIXED_REVIEWS -> s.library.metacriticMixedAverage
    MetacriticRatingTier.UNFAVORABLE -> s.library.metacriticUnfavorable
}

fun BacklogMilestoneReward.localizedSubtitle(s: AppStrings): String {
    if (s.language == AppLanguage.ENGLISH) return subtitle
    return when {
        id.startsWith(BacklogChallengeManager.MILESTONE_BACKLOG_SLAYER) -> "Completa cualquier línea de 3 casillas esta semana (fila, col. o diag.)"
        id.startsWith(BacklogChallengeManager.MILESTONE_ROW_RAIDER) -> "Completa cualquier fila horizontal esta semana"
        id.startsWith(BacklogChallengeManager.MILESTONE_COLUMN_CONQUEROR) -> "Completa cualquier columna vertical esta semana"
        id.startsWith(BacklogChallengeManager.MILESTONE_VAULT_CLEANER) -> "Completa las 9 casillas de la tarjeta semanal (Blackout)"
        id.startsWith(BacklogChallengeManager.MILESTONE_BLACKOUT_CHAMPION) -> "Domina los 9 desafíos en una sola semana"
        else -> subtitle
    }
}
