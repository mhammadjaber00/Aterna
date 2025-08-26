package io.yavero.aterna.features.quest.screen

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
actual fun rememberEnsureTimerPermissions(): suspend () -> Boolean {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var notifRequest by remember { mutableStateOf<CompletableDeferred<Boolean>?>(null) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifRequest?.complete(granted)
        notifRequest = null
    }

    suspend fun requestNotificationsIfNeeded(): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        val has = ctx.checkSelfPermissionCompat(Manifest.permission.POST_NOTIFICATIONS)
        if (has) return true

        val deferred = CompletableDeferred<Boolean>()
        notifRequest = deferred
        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        return deferred.await()
    }

    suspend fun requestExactAlarmIfNeeded(): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        if (ctx.canScheduleExactAlarmsCompat()) return true

        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = "package:${ctx.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)

        return suspendCancellableCoroutine { cont ->
            val obs = object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_RESUME && cont.isActive) {
                        lifecycleOwner.lifecycle.removeObserver(this)
                        cont.resume(ctx.canScheduleExactAlarmsCompat())
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(obs)
            cont.invokeOnCancellation { lifecycleOwner.lifecycle.removeObserver(obs) }
        }
    }

    return {
        val notifOk = requestNotificationsIfNeeded()
        if (!notifOk) false else requestExactAlarmIfNeeded()
    }
}

private fun Context.checkSelfPermissionCompat(name: String): Boolean =
    checkSelfPermission(name) == PackageManager.PERMISSION_GRANTED

private fun Context.canScheduleExactAlarmsCompat(): Boolean =
    if (Build.VERSION.SDK_INT >= 31)
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
    else true