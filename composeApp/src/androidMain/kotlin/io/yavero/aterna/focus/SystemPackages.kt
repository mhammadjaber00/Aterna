package io.yavero.aterna.focus

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import io.yavero.aterna.focus.SystemPackages.build

/**
 * Builds a conservative set of system packages and exposes predicate helpers.
 *
 * The service *never* blocks:
 *  - your own package
 *  - packages identified as system/updated-system
 *  - Settings, SystemUI, installer/permission UIs, Play services/store
 *  - installed home/launcher packages
 */
internal object SystemPackages {

    private const val TAG = "DeepFocus"

    /** Small static seed that covers core system UIs consistently across vendors. */
    private val seedAlwaysSystem = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.android.settings.intelligence",
        "com.android.vending",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        // Some common mainline/system modules that can present system UI
        "com.android.documentsui",
        "com.android.resolv",
        "com.android.captiveportallogin"
    )

    /** Known launchers by brand, in case querying HOME is limited by visibility. */
    private val wellKnownLaunchersSeed = setOf(
        "com.android.launcher3",
        "com.google.android.apps.nexuslauncher",
        "com.sec.android.app.launcher",
        "com.miui.home",
        "com.huawei.android.launcher",
        "com.oppo.launcher",
        "com.bbk.launcher2",
        "net.oneplus.launcher"
    )

    // Cache of HOME packages (rebuilt when we do build())
    @Volatile
    private var cachedHomePkgs: Set<String> = emptySet()

    /**
     * Build a broad set of "system-ish" packages. We try to enumerate, but even if package
     * visibility is restricted, the returned set will still be generous thanks to seeds.
     */
    fun build(pm: PackageManager): Set<String> {
        val out = mutableSetOf<String>()
        out += seedAlwaysSystem
        val homes = installedHomePackages(pm)
        cachedHomePkgs = homes
        out += homes
        out += wellKnownLaunchersSeed

        Log.d(
            TAG,
            "PKGS/BUILD seeds=${seedAlwaysSystem.size} homes=${homes.size} knownLaunchers=${wellKnownLaunchersSeed.size}"
        )

        // Best-effort enumeration of all installed system/updated-system apps
        val installed = try {
            if (Build.VERSION.SDK_INT >= 33) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledApplications(PackageManager.GET_META_DATA)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "PKGS/BUILD getInstalledApplications failed: ${t.message}")
            emptyList<ApplicationInfo>()
        }

        var sysCount = 0
        installed.forEach { ai ->
            val isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdated = (ai.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            if (isSystem || isUpdated) {
                out += ai.packageName
                sysCount++
            }
        }
        Log.i(TAG, "PKGS/BUILD result size=${out.size} (from installed system=$sysCount)")

        return out
    }

    /** Resolve launcher packages via an intent query (works without QUERY_ALL_PACKAGES). */
    private fun installedHomePackages(pm: PackageManager): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val homes = try {
            if (Build.VERSION.SDK_INT >= 33) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, 0)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "PKGS/HOME query failed: ${t.message}")
            emptyList()
        }
        val set = homes.mapNotNull { it.activityInfo?.packageName }.toSet()
        Log.d(TAG, "PKGS/HOME discovered=${set.size} -> $set")
        return set
    }

    /**
     * True if [pkg] is either your own package or a "system" package by flags or membership.
     *
     * @param selfPackage your applicationId
     * @param systemSet runtime-built set from [build]
     */
    fun isSystemOrSelf(
        pm: PackageManager,
        pkg: String,
        selfPackage: String,
        systemSet: Set<String>
    ): Boolean {
        if (pkg == selfPackage) {
            Log.v(TAG, "PKGS/CHECK pkg=$pkg -> SELF")
            return true
        }

        val ai = try {
            if (Build.VERSION.SDK_INT >= 33) {
                pm.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(pkg, 0)
            }
        } catch (_: Throwable) {
            null
        }

        val flagSaysSystem = ai?.let {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                    (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        } ?: false

        val inSet = pkg in systemSet
        val inSeed = pkg in seedAlwaysSystem


        Log.v(TAG, "PKGS/CHECK pkg=$pkg flagSystem=$flagSaysSystem inSet=$inSet inSeed=$inSeed")
        return flagSaysSystem || inSet || inSeed
    }

    /** True if the package is (likely) a launcher/home app. */
    fun isLauncher(pm: PackageManager, pkg: String): Boolean {
        val isHome = cachedHomePkgs.contains(pkg)
        val isSeed = wellKnownLaunchersSeed.contains(pkg)
        val looksLike = pkg.contains("launcher", ignoreCase = true) // last-ditch heuristic
        val result = isHome || isSeed || looksLike
        Log.v(TAG, "PKGS/IS_LAUNCHER pkg=$pkg home=$isHome seed=$isSeed looksLike=$looksLike -> $result")
        return result
    }
}