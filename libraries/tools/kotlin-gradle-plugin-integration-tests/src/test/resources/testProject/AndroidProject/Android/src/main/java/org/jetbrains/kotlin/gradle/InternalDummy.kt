package org.jetbrains.kotlin.gradle

internal class InternalDummy(private konst name: String) {
    internal konst greeting: String
            get() = "Hello $name!"
}