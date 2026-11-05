package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.data.model.UserProfile
import com.example.preferences.AppPreferences
import java.io.File
import java.io.FileOutputStream

object UserProfileManager {
    const val AVATAR_FILE_NAME = "user_profile_avatar.jpg"

    fun getProfile(preferences: AppPreferences): UserProfile {
        return UserProfile(
            name = preferences.userProfileName,
            avatarPath = preferences.userProfileAvatarPath,
            hasCustomTrophySelection = preferences.hasCustomTrophySelection,
            displayedTrophyKeys = preferences.displayedTrophyKeys
        )
    }

    fun saveDisplayedTrophies(
        preferences: AppPreferences,
        hasCustom: Boolean,
        keys: Set<String>
    ): UserProfile {
        preferences.hasCustomTrophySelection = hasCustom
        preferences.displayedTrophyKeys = keys
        return getProfile(preferences)
    }

    fun saveProfile(
        context: Context,
        preferences: AppPreferences,
        name: String,
        avatarUri: Uri? = null,
        removeAvatar: Boolean = false
    ): UserProfile {
        preferences.userProfileName = name.trim()

        if (removeAvatar) {
            deleteAvatarFile(context)
            preferences.userProfileAvatarPath = ""
        } else if (avatarUri != null) {
            val savedPath = processAndSaveAvatar(context, avatarUri)
            if (savedPath != null) {
                preferences.userProfileAvatarPath = savedPath
            }
        }

        return getProfile(preferences)
    }

    fun processAndSaveAvatar(context: Context, uri: Uri): String? {
        return runCatching {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(input) ?: return null
            input.close()

            val minDim = minOf(original.width, original.height)
            val xOffset = (original.width - minDim) / 2
            val yOffset = (original.height - minDim) / 2
            val cropped = Bitmap.createBitmap(original, xOffset, yOffset, minDim, minDim)

            val scaled = if (minDim > 512) {
                Bitmap.createScaledBitmap(cropped, 512, 512, true)
            } else {
                cropped
            }

            val file = File(context.filesDir, AVATAR_FILE_NAME)
            FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            if (scaled != original && scaled != cropped) scaled.recycle()
            if (cropped != original) cropped.recycle()
            original.recycle()

            file.absolutePath
        }.getOrNull()
    }

    fun getAvatarBase64(context: Context, avatarPath: String): String {
        return runCatching {
            val file = if (avatarPath.isNotBlank()) File(avatarPath) else File(context.filesDir, AVATAR_FILE_NAME)
            if (file.exists() && file.length() > 0) {
                val bytes = file.readBytes()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else ""
        }.getOrDefault("")
    }

    fun saveAvatarFromBase64(context: Context, base64: String): String? {
        return runCatching {
            if (base64.isBlank()) return null
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val file = File(context.filesDir, AVATAR_FILE_NAME)
            FileOutputStream(file).use { out ->
                out.write(bytes)
            }
            file.absolutePath
        }.getOrNull()
    }

    fun deleteAvatarFile(context: Context): Boolean {
        val file = File(context.filesDir, AVATAR_FILE_NAME)
        return if (file.exists()) file.delete() else false
    }
}
