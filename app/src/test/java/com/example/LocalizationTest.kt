package com.example

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GiveawayItem
import com.example.i18n.AppLanguage
import com.example.i18n.AppStringsRegistry
import com.example.i18n.LocalAppStrings
import com.example.i18n.LocaleAssetCatalog
import com.example.i18n.LocaleSyncHelper
import com.example.i18n.StringsEn
import com.example.i18n.StringsEs
import com.example.i18n.localizedLabel
import com.example.i18n.localizedTitle
import com.example.preferences.AppPreferences
import com.example.ui.GiveawaysViewModel
import com.example.ui.components.GeneralSettingsCategoryContent
import com.example.ui.components.InitialLanguageDialog
import com.example.ui.theme.LootDropTheme
import com.example.util.BacklogChallengeManager
import com.example.util.ReviewModerationFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocalizationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `app preferences persist app language and initial language selection flag`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AppPreferences(context)

        prefs.hasSelectedInitialLanguage = false
        prefs.appLanguage = AppLanguage.ENGLISH
        assertFalse(prefs.hasSelectedInitialLanguage)
        assertEquals(AppLanguage.ENGLISH, prefs.appLanguage)
        assertEquals(AppLanguage.ENGLISH, AppStringsRegistry.forContext(context).language)

        prefs.appLanguage = AppLanguage.SPANISH
        prefs.hasSelectedInitialLanguage = true
        val reloaded = AppPreferences(context)
        assertTrue(reloaded.hasSelectedInitialLanguage)
        assertEquals(AppLanguage.SPANISH, reloaded.appLanguage)
        assertEquals(AppLanguage.SPANISH, AppStringsRegistry.forContext(context).language)
    }

    @Test
    fun `view model completes initial language selection and switches language dynamically`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = AppPreferences(app)
        prefs.hasSelectedInitialLanguage = false
        prefs.appLanguage = AppLanguage.ENGLISH

        val viewModel = GiveawaysViewModel(app)
        assertFalse(viewModel.hasSelectedInitialLanguage.value)
        assertEquals(AppLanguage.ENGLISH, viewModel.appLanguage.value)

        viewModel.completeInitialLanguageSelection(AppLanguage.SPANISH)
        assertTrue(viewModel.hasSelectedInitialLanguage.value)
        assertEquals(AppLanguage.SPANISH, viewModel.appLanguage.value)
        assertEquals(AppLanguage.SPANISH, AppPreferences(app).appLanguage)

        viewModel.setAppLanguage(AppLanguage.ENGLISH)
        assertEquals(AppLanguage.ENGLISH, viewModel.appLanguage.value)
        assertEquals(AppLanguage.ENGLISH, AppPreferences(app).appLanguage)
    }

    @Test
    fun `initial language dialog switches preview to Spanish and confirms chosen language`() {
        var previewed: AppLanguage? = null
        var confirmed: AppLanguage? = null

        composeTestRule.setContent {
            LootDropTheme {
                InitialLanguageDialog(
                    initialLanguage = AppLanguage.ENGLISH,
                    onSelectLanguagePreview = { previewed = it },
                    onConfirmLanguage = { confirmed = it }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("initial_language_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("initial_lang_option_es").performClick()
        composeTestRule.waitForIdle()

        assertEquals(AppLanguage.SPANISH, previewed)
        composeTestRule.onNodeWithTag("initial_lang_confirm_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(StringsEs.common.initialLangConfirmEs).assertExists()

        composeTestRule.onNodeWithTag("initial_lang_confirm_button").performClick()
        composeTestRule.waitForIdle()

        assertEquals(AppLanguage.SPANISH, confirmed)
    }

    @Test
    fun `settings general language changer updates UI copy and preference`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        AppPreferences(app).apply {
            appLanguage = AppLanguage.ENGLISH
            hasSelectedInitialLanguage = true
        }
        val viewModel = GiveawaysViewModel(app)

        composeTestRule.setContent {
            val lang by viewModel.appLanguage.collectAsState()
            LootDropTheme {
                CompositionLocalProvider(LocalAppStrings provides AppStringsRegistry.forLanguage(lang)) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        GeneralSettingsCategoryContent(viewModel = viewModel)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("settings_language_section").assertIsDisplayed()
        composeTestRule.onNodeWithText(StringsEn.settings.generalDisplayBehaviorSection).assertExists()

        composeTestRule.onNodeWithTag("settings_language_es").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(AppLanguage.SPANISH, viewModel.appLanguage.value)
        composeTestRule.onNodeWithText(StringsEs.settings.generalDisplayBehaviorSection).assertExists()

        composeTestRule.onNodeWithTag("settings_language_en").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(AppLanguage.ENGLISH, viewModel.appLanguage.value)
        composeTestRule.onNodeWithText(StringsEn.settings.generalDisplayBehaviorSection).assertExists()
    }

    @Test
    fun `spanish string resources, launcher options, challenge weeks, and moderation localize accurately`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val esContext = LocaleSyncHelper.localizedContext(context, AppLanguage.SPANISH)

        assertEquals("LootDrop", esContext.getString(R.string.app_name))
        assertEquals("RECLAMAR", esContext.getString(R.string.widget_claim_btn))
        assertEquals("DETALLES", esContext.getString(R.string.widget_details_btn))
        assertEquals("MI BÓVEDA", esContext.getString(R.string.widget_vault_btn))

        // Verify Spanish translations differ from English in all major string groups
        assertNotEquals(StringsEn.common.tabHome, StringsEs.common.tabHome)
        assertNotEquals(StringsEn.home.homeHeroSubtitle, StringsEs.home.homeHeroSubtitle)
        assertNotEquals(StringsEn.library.trophiesWallTitle, StringsEs.library.trophiesWallTitle)
        assertNotEquals(StringsEn.dialogs.detailClaimFree, StringsEs.dialogs.detailClaimFree)
        assertNotEquals(StringsEn.settings.aboutPatreonBtn, StringsEs.settings.aboutPatreonBtn)
        assertNotEquals(StringsEn.system.notifActionClaimFree, StringsEs.system.notifActionClaimFree)

        // Backlog week display name in Spanish
        val weekEs = BacklogChallengeManager.getWeekDisplayName("2026-W10", AppLanguage.SPANISH)
        assertTrue(weekEs.contains("Semana"))

        // Giveaway launcher options in Spanish
        val item = GiveawayItem(
            id = 500,
            title = "Control",
            worth = "$29.99",
            numericWorth = 29.99,
            thumbnail = "",
            image = "",
            description = "",
            instructions = "",
            openGiveawayUrl = "https://gamerpower.com/control",
            publishedDate = "",
            type = "Game",
            platforms = "PC, Steam",
            endDate = "N/A",
            users = 100,
            status = "Active"
        )
        val esLaunchers = item.getAvailableLaunchers(isSpanish = true)
        assertTrue(esLaunchers.any { it.name.contains("Página Oficial del Regalo") })

        // Review moderation validation in Spanish
        val validation = ReviewModerationFilter.validateReviewInput(
            title = "",
            comment = "",
            authorName = "Tester",
            isSpanish = true
        )
        assertFalse(validation.isAllowed)
        assertTrue(validation.errorMessage!!.contains("título"))
    }

    @Test
    fun `locale asset catalog loads en and es json asset files for enum and theme labels`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocaleAssetCatalog.clearCacheForTesting()
        LocaleAssetCatalog.preload(context, AppLanguage.ENGLISH)
        LocaleAssetCatalog.preload(context, AppLanguage.SPANISH)

        assertEquals(
            "Rewards (7)",
            com.example.preferences.AccentCategory.REWARDS.localizedLabel(StringsEn)
        )
        assertEquals(
            "Premios (7)",
            com.example.preferences.AccentCategory.REWARDS.localizedLabel(StringsEs)
        )
        assertEquals(
            "Aurora Sovereign",
            com.example.preferences.AccentTheme.AURORA_SOVEREIGN.localizedTitle(StringsEn)
        )
        assertEquals(
            "Aurora Soberana",
            com.example.preferences.AccentTheme.AURORA_SOVEREIGN.localizedTitle(StringsEs)
        )
        assertEquals(
            "OLED Midnight",
            com.example.preferences.BackgroundTheme.OLED_BLACK.localizedTitle(StringsEn)
        )
        assertEquals(
            "Negro OLED",
            com.example.preferences.BackgroundTheme.OLED_BLACK.localizedTitle(StringsEs)
        )
        assertEquals(
            "Newest First",
            com.example.ui.SortOption.NEWEST.localizedLabel(StringsEn)
        )
        assertEquals(
            "Más recientes",
            com.example.ui.SortOption.NEWEST.localizedLabel(StringsEs)
        )
    }
}
