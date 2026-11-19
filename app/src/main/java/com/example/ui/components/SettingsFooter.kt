package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted

@Composable
fun SettingsFooter(
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val lang = s.language
    val context = LocalContext.current
    val patreonUrl = "https://www.patreon.com/cw/LootDrop"

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Patreon Support Circle & Button Card at the bottom of settings
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(patreonUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1B4B).copy(alpha = 0.65f),
            border = BorderStroke(1.dp, Color(0xFFFF424D).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Little circular badge with Patreon logo
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF424D)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                // Community / Support Message
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "¡Sé Parte de la Comunidad!"
                            AppLanguage.FRENCH -> "Rejoignez la Communauté !"
                            AppLanguage.ENGLISH -> "Be Part of the Community!"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "Apoya a LootDrop en Patreon y desbloquea ventajas"
                            AppLanguage.FRENCH -> "Soutenez LootDrop sur Patreon & débloquez des avantages"
                            AppLanguage.ENGLISH -> "Help support LootDrop on Patreon & unlock backer perks"
                        },
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )
                }

                // Support Button opening the link
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(patreonUrl)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF424D),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "Apoyar ❤️"
                            AppLanguage.FRENCH -> "Soutenir ❤️"
                            AppLanguage.ENGLISH -> "Support ❤️"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = when (lang) {
                AppLanguage.SPANISH -> "Logo de LootDrop"
                AppLanguage.FRENCH -> "Logo LootDrop"
                AppLanguage.ENGLISH -> "LootDrop Logo"
            },
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = when (lang) {
                AppLanguage.SPANISH -> "LootDrop V${BuildConfig.VERSION_NAME} • Radar Gratis PC y Consola"
                AppLanguage.FRENCH -> "LootDrop V${BuildConfig.VERSION_NAME} • Radar Jeux Gratuits PC & Console"
                AppLanguage.ENGLISH -> "LootDrop V${BuildConfig.VERSION_NAME} • PC & Console Free Games Radar"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (lang) {
                AppLanguage.SPANISH -> "Datos de ofertas en tiempo real con GamerPower API"
                AppLanguage.FRENCH -> "Données d'offres en temps réel via l'API GamerPower"
                AppLanguage.ENGLISH -> "Real-time giveaways data powered by GamerPower API"
            },
            fontSize = 11.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(NeonGreen.copy(alpha = 0.15f))
                .border(0.5.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = when (lang) {
                    AppLanguage.SPANISH -> "✓ 100% AOSP Puro • Cero Servicios Google (GMS)"
                    AppLanguage.FRENCH -> "✓ 100 % AOSP Pur • Zéro Services Google (GMS)"
                    AppLanguage.ENGLISH -> "✓ 100% Pure AOSP • Zero Google Play Services (GMS)"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NeonGreen
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (lang) {
                AppLanguage.SPANISH -> "Hecho por BIGBKOG"
                AppLanguage.FRENCH -> "Créé par BIGBKOG"
                AppLanguage.ENGLISH -> "Made by BIGBKOG"
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
