package io.yavero.aterna.focus

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.core.net.toUri
import io.yavero.aterna.domain.model.AppSettings
import io.yavero.aterna.domain.repository.SettingsRepository
import kotlinx.coroutines.*
import org.koin.compose.koinInject

// ---------- Constants ----------
private const val ACTION_ACC_DETAILS = "android.settings.ACCESSIBILITY_DETAILS_SETTINGS"
private const val EXTRA_ACC_COMPONENT = "android.provider.extra.EXTRA_ACCESSIBILITY_COMPONENT_NAME"
private const val PREFS = "deep_focus_prefs"           // used by the service
private const val KEY_ENABLED = "deep_focus_enabled"   // service restore toggle

// ---------- Helpers ----------
/** True if the AccessibilityService is enabled in system settings. */
private fun isServiceEnabled(ctx: Context): Boolean {
    val accessibilityOn = Settings.Secure.getInt(
        ctx.contentResolver,
        Settings.Secure.ACCESSIBILITY_ENABLED,
        0
    ) == 1
    if (!accessibilityOn) return false

    val enabledServices = Settings.Secure.getString(
        ctx.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val flat = ComponentName(ctx, DeepFocusAccessibilityService::class.java).flattenToString()
    return enabledServices.split(':').any { it.equals(flat, ignoreCase = true) }
}

private fun labelOf(pm: PackageManager, pkg: String): String =
    try {
        val ai = pm.getApplicationInfo(pkg, 0)
        pm.getApplicationLabel(ai)?.toString() ?: pkg
    } catch (_: Exception) {
        pkg
    }

// ---------- Actuals ----------
@Composable
actual fun rememberDeepFocusPermissionStatus(): DeepFocusPermissionStatus {
    val ctx = LocalContext.current
    return DeepFocusPermissionStatus(accessibilityEnabled = isServiceEnabled(ctx))
}

@Composable
actual fun rememberEnsureDeepFocusPermissions(): suspend () -> Boolean {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* we poll below */ }

    return remember {
        suspend {
            if (isServiceEnabled(ctx)) true

            val comp = ComponentName(ctx, DeepFocusAccessibilityService::class.java)
            val details = Intent(ACTION_ACC_DETAILS).apply {
                data = "package:${ctx.packageName}".toUri()
                putExtra(EXTRA_ACC_COMPONENT, comp.flattenToString())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val generic = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                launcher.launch(details)
            } catch (_: Exception) {
                launcher.launch(generic)
            }

            withContext(Dispatchers.Default) {
                withTimeoutOrNull(45_000L) {
                    while (true) {
                        if (isServiceEnabled(ctx)) return@withTimeoutOrNull true
                        delay(750L)
                    }
                    @Suppress("UNREACHABLE_CODE")
                    false
                } ?: false
            }
        }
    }
}

@Composable
actual fun rememberApplyDeepFocusSession(): (Boolean) -> Unit {
    val appCtx = LocalContext.current.applicationContext
    return remember {
        { enabled ->
            DeepFocusContract.setEnabled(appCtx, enabled)
            appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit { putBoolean(KEY_ENABLED, enabled) } // service restore
        }
    }
}

@Composable
actual fun rememberUpdateDeepFocusAllowlist(): (Set<String>) -> Unit {
    val appCtx = LocalContext.current.applicationContext
    return remember {
        { pkgs -> DeepFocusContract.updateAllowlist(appCtx, pkgs) }
    }
}

/** Launchable, non-system apps (excluding your own), sorted by label. */
@Composable
actual fun rememberInstalledApps(): List<InstalledApp> {
    val ctx = LocalContext.current
    val pm = ctx.packageManager
    val self = ctx.packageName

    return remember {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0)
            .mapNotNull { it.activityInfo?.packageName }
            .distinct()
            .filter { it != self }
            .filter { pkg ->
                try {
                    val ai = pm.getApplicationInfo(pkg, 0)
                    (ai.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0
                } catch (_: Exception) {
                    false
                }
            }
            .map { pkg -> InstalledApp(pkg, labelOf(pm, pkg)) }
            .sortedBy { it.label.lowercase() }
    }
}

/** Load allowlist via your SettingsRepository (KMM Settings under the hood). */
@Composable
actual fun rememberLoadDeepFocusAllowlist(): Set<String> {
    val repo: SettingsRepository = koinInject()
    val appSettings by repo.getAppSettings().collectAsState(initial = AppSettings())
    return appSettings.deepFocusAllowlist
}

/** Save allowlist via your SettingsRepository. Broadcast separately when needed. */
@Composable
actual fun rememberSaveDeepFocusAllowlist(): (Set<String>) -> Unit {
    val repo: SettingsRepository = koinInject()
    val scope = rememberCoroutineScope()
    return remember {
        { pkgs -> scope.launch { repo.setDeepFocusAllowlist(pkgs) } }
    }
}