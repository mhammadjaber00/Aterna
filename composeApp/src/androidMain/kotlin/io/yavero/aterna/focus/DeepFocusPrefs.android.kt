package io.yavero.aterna.focus

import com.russhwolf.settings.Settings

actual object DeepFocusPrefs {
    private val settings by lazy { Settings() }
    private const val KEY = "deep_focus_desired"

    actual fun getDesired(): Boolean = settings.getBoolean(KEY, false)
    actual fun setDesired(value: Boolean) {
        settings.putBoolean(KEY, value)
    }
}