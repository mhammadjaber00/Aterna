package io.yavero.aterna.focus

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

// Deep-links for Accessibility settings (details view if available)
private const val ACTION_ACC_DETAILS = "android.settings.ACCESSIBILITY_DETAILS_SETTINGS"
private const val EXTRA_ACC_COMPONENT = "android.provider.extra.EXTRA_ACCESSIBILITY_COMPONENT_NAME"

/**
 * Returns true if the DeepFocusAccessibilityService is enabled in system Accessibility settings.
 */
private fun isServiceEnabled(ctx: Context): Boolean {
    val enabled = Settings.Secure.getInt(
        ctx.contentResolver,
        Settings.Secure.ACCESSIBILITY_ENABLED,
        0
    ) == 1
    if (!enabled) return false

    val setting = Settings.Secure.getString(
        ctx.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val flat = ComponentName(ctx, DeepFocusAccessibilityService::class.java).flattenToString()
    return setting.split(':').any { it.equals(flat, ignoreCase = true) }
}

/**
 * Lightweight snapshot of permission state consumed by your UI.
 * (Expected type from your common module.)
 */
@Composable
actual fun rememberDeepFocusPermissionStatus(): DeepFocusPermissionStatus {
    val ctx = LocalContext.current
    return DeepFocusPermissionStatus(
        accessibilityEnabled = isServiceEnabled(ctx)
    )
}

/**
 * Presents system Accessibility Settings (deep-linked to your service when supported),
 * then polls for enablement for up to ~45s. Returns true as soon as the service is enabled.
 *
 * Safe on all vendors: falls back to the generic Accessibility screen when details action
 * is unavailable.
 */

@Composable
actual fun rememberEnsureDeepFocusPermissions(): suspend () -> Boolean {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* result not needed; we poll below */ }

    return remember {
        // explicit type keeps inference honest
        val ensure: suspend () -> Boolean = {
            if (isServiceEnabled(ctx)) {
                true
            } else {
                val comp = ComponentName(ctx, DeepFocusAccessibilityService::class.java)
                val details = Intent(ACTION_ACC_DETAILS).apply {
                    data = Uri.parse("package:${ctx.packageName}")
                    putExtra(EXTRA_ACC_COMPONENT, comp.flattenToString())
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val generic = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                // open settings (details first, fallback to generic)
                try {
                    launcher.launch(details)
                } catch (_: Exception) {
                    launcher.launch(generic)
                }

                // poll up to 45s, every 750ms
                withContext(Dispatchers.Default) {
                    (withTimeoutOrNull<Boolean>(45_000L) {
                        while (true) {
                            if (isServiceEnabled(ctx)) return@withTimeoutOrNull true
                            delay(750L)
                        }
                        false
                    } ?: false)
                }
            }
        }
        ensure
    }
}

/**
 * Applies (starts/stops) a Deep Focus session by broadcasting through the in-app contract.
 * Also mirrors the value in local prefs so the UI can render immediately while the service
 * processes the broadcast.
 */
@Composable
actual fun rememberApplyDeepFocusSession(): (Boolean) -> Unit {
    val appCtx = LocalContext.current.applicationContext
    return remember {
        { enabled ->
            // Use the shared contract so constants stay in one place.
            DeepFocusContract.setEnabled(appCtx, enabled)

            // Mirror to local prefs for instant UI reflect (service also persists its own copy).
            appCtx.getSharedPreferences("deep_focus_prefs", Context.MODE_PRIVATE)
                .edit {
                    putBoolean("deep_focus_enabled", enabled)
                }
        }
    }
}

/**
 * (Optional helper) Send a new allowlist to the service. Your own package is always
 * implicitly allowlisted by the service — only include *extra* packages here.
 */
@Composable
fun rememberUpdateDeepFocusAllowlist(): (Set<String>) -> Unit {
    val appCtx = LocalContext.current.applicationContext
    return remember {
        { pkgs ->
            DeepFocusContract.updateAllowlist(appCtx, pkgs)
        }
    }
}
