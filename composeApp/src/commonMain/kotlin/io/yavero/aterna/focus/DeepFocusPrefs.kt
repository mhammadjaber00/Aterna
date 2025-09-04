package io.yavero.aterna.focus

expect object DeepFocusPrefs {
    fun getDesired(): Boolean
    fun setDesired(value: Boolean)
}