package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.notifications.NotificationHelper
import com.example.ui.GiveawaysViewModel
import com.example.ui.HomeScreen
import com.example.ui.theme.LootDropTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GiveawaysViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val accentTheme by viewModel.accentTheme.collectAsState()
            val backgroundTheme by viewModel.backgroundTheme.collectAsState()

            LootDropTheme(
                accentTheme = accentTheme,
                backgroundTheme = backgroundTheme
            ) {
                HomeScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val giveawayId = intent.getIntExtra(NotificationHelper.EXTRA_GIVEAWAY_ID, -1)
        if (giveawayId != -1) {
            viewModel.openItemById(giveawayId)
        }
    }
}
