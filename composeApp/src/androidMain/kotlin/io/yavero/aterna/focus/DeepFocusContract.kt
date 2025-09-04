package io.yavero.aterna.focus

import android.content.Context
import android.content.Intent
import android.util.Log
import io.yavero.aterna.focus.DeepFocusContract.ACTION_DEEP_FOCUS_ALLOWLIST
import io.yavero.aterna.focus.DeepFocusContract.ACTION_DEEP_FOCUS_STATE

/**
 * Contract for controlling Deep Focus via broadcasts.
 *
 * Usage:
 *   DeepFocusContract.setEnabled(context, true)
 *   DeepFocusContract.updateAllowlist(context, setOf("com.whatever", "com.foo"))
 */
object DeepFocusContract {

    private const val TAG = "DeepFocus"

    /** Toggle the Deep Focus session. */
    const val ACTION_DEEP_FOCUS_STATE = "io.yavero.aterna.action.DEEP_FOCUS_STATE"

    /** Extra boolean for [ACTION_DEEP_FOCUS_STATE]. */
    const val EXTRA_ENABLED = "enabled"

    /** Update/replace allowlisted packages at runtime (merged with defaults). */
    const val ACTION_DEEP_FOCUS_ALLOWLIST = "io.yavero.aterna.action.DEEP_FOCUS_ALLOWLIST"

    /** String[] extra for [ACTION_DEEP_FOCUS_ALLOWLIST]. */
    const val EXTRA_PACKAGES = "packages"

    fun setEnabled(context: Context, enabled: Boolean) {
        Log.d(TAG, "CONTRACT setEnabled($enabled) -> broadcast")
        val i = Intent(ACTION_DEEP_FOCUS_STATE)
            .setPackage(context.packageName)
            .putExtra(EXTRA_ENABLED, enabled)
            .addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
        context.sendBroadcast(i)
    }

    fun updateAllowlist(context: Context, packages: Set<String>) {
        Log.d(TAG, "CONTRACT updateAllowlist size=${packages.size} pkgs=$packages")
        val i = Intent(ACTION_DEEP_FOCUS_ALLOWLIST)
            .setPackage(context.packageName)
            .putExtra(EXTRA_PACKAGES, packages.toTypedArray())
            .addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
        context.sendBroadcast(i)
    }
}