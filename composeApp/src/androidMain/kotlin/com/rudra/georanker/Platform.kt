package com.rudra.georanker


import android.os.Build

object AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
}

actual fun getPlatform(): Platform = AndroidPlatform