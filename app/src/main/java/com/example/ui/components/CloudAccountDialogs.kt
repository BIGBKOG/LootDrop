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
    val lang = s.language
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
                    text = when (lang) {
                        AppLanguage.SPANISH -> "Configura tu cuenta gamer y contraseña (mín. 6 caracteres). Tu nombre coincidirá con tu perfil y quedará reservado."
                        AppLanguage.FRENCH -> "Configurez votre compte joueur et mot de passe (min. 6 caractères). Votre pseudo correspondra à votre profil et sera réservé."
                        AppLanguage.ENGLISH -> "Set up your gamer account and password (min 6 characters). Your display name will match your profile name and be reserved so nobody else can use it."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = inputUser,
                    onValueChange = { inputUser = it; errorMessage = null },
                    label = {
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "Gamer Tag / Nombre de Perfil"
                                AppLanguage.FRENCH -> "Gamer Tag / Nom de profil"
                                AppLanguage.ENGLISH -> "Gamer Tag / Profile Name"
                            }
                        )
                    },
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
                                contentDescription = when (lang) {
                                    AppLanguage.SPANISH -> "Mostrar u ocultar contraseña"
                                    AppLanguage.FRENCH -> "Afficher ou masquer le mot de passe"
                                    AppLanguage.ENGLISH -> "Toggle password"
                                }
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
                    label = {
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "Confirmar Contraseña"
                                AppLanguage.FRENCH -> "Confirmer le mot de passe"
                                AppLanguage.ENGLISH -> "Confirm Password"
                            }
                        )
                    },
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
                        errorMessage = when (lang) {
                            AppLanguage.SPANISH -> "El nombre de cuenta debe tener al menos 3 caracteres."
                            AppLanguage.FRENCH -> "Le nom du compte doit contenir au moins 3 caractères."
                            AppLanguage.ENGLISH -> "Account name must be at least 3 characters."
                        }
                        return@Button
                    }
                    if (e.isBlank() || !e.contains("@") || !e.contains(".")) {
                        errorMessage = when (lang) {
                            AppLanguage.SPANISH -> "Introduce un correo electrónico válido (ej. nombre@ejemplo.com)."
                            AppLanguage.FRENCH -> "Veuillez saisir une adresse e-mail valide (ex. nom@exemple.com)."
                            AppLanguage.ENGLISH -> "Please enter a valid email address (e.g. name@example.com)."
                        }
                        return@Button
                    }
                    if (p.length < 6) {
                        errorMessage = when (lang) {
                            AppLanguage.SPANISH -> "La contraseña debe tener al menos 6 caracteres."
                            AppLanguage.FRENCH -> "Le mot de passe doit contenir au moins 6 caractères."
                            AppLanguage.ENGLISH -> "Password must be at least 6 characters."
                        }
                        return@Button
                    }
                    if (p != inputConfirmPass) {
                        errorMessage = when (lang) {
                            AppLanguage.SPANISH -> "Las contraseñas no coinciden."
                            AppLanguage.FRENCH -> "Les mots de passe ne correspondent pas."
                            AppLanguage.ENGLISH -> "Passwords do not match."
                        }
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
                    Text(
                        when (lang) {
                            AppLanguage.SPANISH -> "Crear y Conectar"
                            AppLanguage.FRENCH -> "Créer et connecter"
                            AppLanguage.ENGLISH -> "Create & Connect"
                        },
                        fontWeight = FontWeight.Bold
                    )
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
    val lang = s.language
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
                    text = when (lang) {
                        AppLanguage.SPANISH -> "Iniciar Sesión en Nube"
                        AppLanguage.FRENCH -> "Connexion au Cloud"
                        AppLanguage.ENGLISH -> "Sign In to Cloud Backup"
                    },
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
                    text = when (lang) {
                        AppLanguage.SPANISH -> "Ingresa tu cuenta y contraseña para conectar tu archivo de respaldo personal."
                        AppLanguage.FRENCH -> "Saisissez votre compte et mot de passe pour connecter votre sauvegarde personnelle."
                        AppLanguage.ENGLISH -> "Enter your account and password to connect your personal backup file."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = inputUser,
                    onValueChange = { inputUser = it; errorMessage = null },
                    label = {
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "Nombre de Cuenta o Correo"
                                AppLanguage.FRENCH -> "Nom du compte ou e-mail"
                                AppLanguage.ENGLISH -> "Account Name or Email"
                            }
                        )
                    },
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
                                contentDescription = when (lang) {
                                    AppLanguage.SPANISH -> "Mostrar u ocultar contraseña"
                                    AppLanguage.FRENCH -> "Afficher ou masquer le mot de passe"
                                    AppLanguage.ENGLISH -> "Toggle password"
                                }
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
                            text = when (lang) {
                                AppLanguage.SPANISH -> "¿No tienes cuenta? Crea una"
                                AppLanguage.FRENCH -> "Pas de compte ? Créez-en un"
                                AppLanguage.ENGLISH -> "Don't have an account? Create one"
                            },
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
                        errorMessage = when (lang) {
                            AppLanguage.SPANISH -> "Ingresa tu usuario y contraseña."
                            AppLanguage.FRENCH -> "Veuillez saisir votre identifiant et mot de passe."
                            AppLanguage.ENGLISH -> "Please enter your username and password."
                        }
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
    val lang = s.language
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
                Text(
                    when (lang) {
                        AppLanguage.SPANISH -> "¿Restaurar Respaldo de Cuenta?"
                        AppLanguage.FRENCH -> "Restaurer la sauvegarde du compte ?"
                        AppLanguage.ENGLISH -> "Restore Account Backup?"
                    }
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = when (lang) {
                        AppLanguage.SPANISH -> "Esto buscará y descargará el respaldo vinculado específicamente a la cuenta '$username'."
                        AppLanguage.FRENCH -> "Cela recherchera et téléchargera la sauvegarde liée au compte « $username »."
                        AppLanguage.ENGLISH -> "This will locate and download the backup specifically linked to account '$username'."
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (lang) {
                        AppLanguage.SPANISH -> "• Cuenta: $username\n• Conserva datos locales al combinar respaldos\n• Restaura juegos reclamados, estados y perfil"
                        AppLanguage.FRENCH -> "• Compte : $username\n• Préserve les données locales lors de la fusion\n• Restaure jeux réclamés, statuts et profil"
                        AppLanguage.ENGLISH -> "• Account: $username\n• Preserves local data while merging cloud saves\n• Restores claimed games, play statuses, and profile"
                    },
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
                Text(
                    when (lang) {
                        AppLanguage.SPANISH -> "Restaurar de la Cuenta"
                        AppLanguage.FRENCH -> "Restaurer du compte"
                        AppLanguage.ENGLISH -> "Restore from Account"
                    },
                    fontWeight = FontWeight.Bold
                )
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
    val lang = s.language
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
                text = when (lang) {
                    AppLanguage.SPANISH -> "¿Cerrar sesión de la cuenta '$username'? Tus juegos guardados y datos locales seguirán intactos, pero la sincronización en la nube se pausará hasta volver a entrar."
                    AppLanguage.FRENCH -> "Déconnecter le compte « $username » ? Vos jeux enregistrés et données locales resteront intacts, mais la synchro cloud sera en pause jusqu'à votre reconnexion."
                    AppLanguage.ENGLISH -> "Sign out of account '$username'? Your local saved giveaways and device data will stay intact, but cloud sync and backups will be paused until you sign back in."
                },
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
