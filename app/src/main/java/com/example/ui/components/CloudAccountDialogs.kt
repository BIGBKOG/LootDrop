package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.GiveawaysViewModel
import com.example.ui.theme.NeonCyan

@Composable
internal fun CreateCloudAccountDialog(
    viewModel: GiveawaysViewModel,
    context: Context,
    username: String,
    isAuthenticating: Boolean,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val currentProfileName = viewModel.userProfile.collectAsState().value.name
    var inputUser by remember { mutableStateOf(username.ifBlank { currentProfileName.ifBlank { "" } }) }
    var inputEmail by remember { mutableStateOf("") }
    var inputPass by remember { mutableStateOf("") }
    var inputConfirmPass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isAuthenticating) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(s.settings.cloudCreateAccountBtn, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isEs) "Configura tu cuenta gamer y contraseña (mín. 6 caracteres). Tu nombre coincidirá con tu perfil y quedará reservado." else "Set up your gamer account and password (min 6 characters). Your display name will match your profile name and be reserved so nobody else can use it.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = inputUser,
                    onValueChange = { inputUser = it; errorMessage = null },
                    label = { Text(if (isEs) "Gamer Tag / Nombre de Perfil" else "Gamer Tag / Profile Name") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_create_account_user"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = inputEmail,
                    onValueChange = { inputEmail = it; errorMessage = null },
                    label = { Text(s.settings.cloudEmailLabel) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_create_account_email"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = inputPass,
                    onValueChange = { inputPass = it; errorMessage = null },
                    label = { Text(s.settings.cloudPasswordLabel) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isEs) "Mostrar u ocultar contraseña" else "Toggle password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_create_account_pass"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = inputConfirmPass,
                    onValueChange = { inputConfirmPass = it; errorMessage = null },
                    label = { Text(if (isEs) "Confirmar Contraseña" else "Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_create_account_confirm_pass"),
                    shape = RoundedCornerShape(10.dp)
                )

                errorMessage?.let { err ->
                    Text(
                        text = err,
                        fontSize = 11.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val u = inputUser.trim()
                    val e = inputEmail.trim()
                    val p = inputPass
                    if (u.length < 3) {
                        errorMessage = if (isEs) "El nombre de cuenta debe tener al menos 3 caracteres." else "Account name must be at least 3 characters."
                        return@Button
                    }
                    if (e.isBlank() || !e.contains("@") || !e.contains(".")) {
                        errorMessage = if (isEs) "Introduce un correo electrónico válido (ej. nombre@ejemplo.com)." else "Please enter a valid email address (e.g. name@example.com)."
                        return@Button
                    }
                    if (p.length < 6) {
                        errorMessage = if (isEs) "La contraseña debe tener al menos 6 caracteres." else "Password must be at least 6 characters."
                        return@Button
                    }
                    if (p != inputConfirmPass) {
                        errorMessage = if (isEs) "Las contraseñas no coinciden." else "Passwords do not match."
                        return@Button
                    }
                    viewModel.createCloudAccount(u, p, context, email = e) { success, msg ->
                        if (success) onDismiss() else errorMessage = msg
                    }
                },
                enabled = !isAuthenticating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_submit_create_account")
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.background, modifier = Modifier.size(16.dp))
                } else {
                    Text(if (isEs) "Crear y Conectar" else "Create & Connect", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isAuthenticating) {
                Text(s.common.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
internal fun SignInCloudAccountDialog(
    viewModel: GiveawaysViewModel,
    context: Context,
    username: String,
    isAuthenticating: Boolean,
    onDismiss: () -> Unit,
    onSwitchToCreateAccount: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    var inputUser by remember { mutableStateOf(username) }
    var inputEmail by remember { mutableStateOf("") }
    var inputPass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isAuthenticating) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = if (isEs) "Iniciar Sesión en Nube" else "Sign In to Cloud Backup",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isEs) "Ingresa tu cuenta y contraseña para conectar tu archivo de respaldo personal." else "Enter your account and password to connect your personal backup file.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = inputUser,
                    onValueChange = { inputUser = it; errorMessage = null },
                    label = { Text(if (isEs) "Nombre de Cuenta o Correo" else "Account Name or Email") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_signin_user"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = inputEmail,
                    onValueChange = { inputEmail = it; errorMessage = null },
                    label = { Text(s.settings.cloudEmailLabel) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_signin_email"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = inputPass,
                    onValueChange = { inputPass = it; errorMessage = null },
                    label = { Text(s.settings.cloudPasswordLabel) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isEs) "Mostrar u ocultar contraseña" else "Toggle password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_signin_pass"),
                    shape = RoundedCornerShape(10.dp)
                )

                errorMessage?.let { err ->
                    Text(
                        text = err,
                        fontSize = 11.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onSwitchToCreateAccount) {
                        Text(
                            text = if (isEs) "¿No tienes cuenta? Crea una" else "Don't have an account? Create one",
                            fontSize = 11.sp,
                            color = NeonCyan
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val u = inputUser.trim()
                    val e = inputEmail.trim()
                    val p = inputPass
                    if (u.isBlank() || p.isBlank()) {
                        errorMessage = if (isEs) "Ingresa tu usuario y contraseña." else "Please enter your username and password."
                        return@Button
                    }
                    viewModel.signInCloudAccount(u, p, context, email = e) { success, msg ->
                        if (success) onDismiss() else errorMessage = msg
                    }
                },
                enabled = !isAuthenticating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_submit_sign_in")
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.background, modifier = Modifier.size(16.dp))
                } else {
                    Text(s.settings.cloudSignInBtn, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isAuthenticating) {
                Text(s.common.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
internal fun RestoreCloudConfirmDialog(
    username: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(if (isEs) "¿Restaurar Respaldo de Cuenta?" else "Restore Account Backup?")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isEs) "Esto buscará y descargará el respaldo vinculado específicamente a la cuenta '$username'." else "This will locate and download the backup specifically linked to account '$username'.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isEs) "• Cuenta: $username\n• Conserva datos locales al combinar respaldos\n• Restaura juegos reclamados, estados y perfil" else "• Account: $username\n• Preserves local data while merging cloud saves\n• Restores claimed games, play statuses, and profile",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_confirm_account_restore")
            ) {
                Text(if (isEs) "Restaurar de la Cuenta" else "Restore from Account", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.common.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
internal fun SignOutCloudConfirmDialog(
    username: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(s.settings.cloudSignOutBtn, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Text(
                text = if (isEs) "¿Cerrar sesión de la cuenta '$username'? Tus juegos guardados y datos locales seguirán intactos, pero la sincronización en la nube se pausará hasta volver a entrar." else "Sign out of account '$username'? Your local saved giveaways and device data will stay intact, but cloud sync and backups will be paused until you sign back in.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_sign_out_dialog_btn")
            ) {
                Text(s.settings.cloudSignOutBtn, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.common.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}
