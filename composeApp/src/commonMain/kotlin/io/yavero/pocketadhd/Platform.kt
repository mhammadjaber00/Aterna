package io.yavero.pocketadhd

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform