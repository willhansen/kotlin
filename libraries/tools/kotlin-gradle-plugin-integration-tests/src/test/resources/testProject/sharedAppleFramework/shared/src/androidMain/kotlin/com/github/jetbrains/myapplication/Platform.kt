package com.github.jetbrains.myapplication

actual class Platform actual constructor() {
    actual konst platform: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}