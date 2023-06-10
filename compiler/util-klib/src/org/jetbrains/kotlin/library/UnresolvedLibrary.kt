@file:Suppress("FunctionName")

package org.jetbrains.kotlin.library

fun UnresolvedLibrary(path: String, libraryVersion: String?): RequiredUnresolvedLibrary =
    RequiredUnresolvedLibrary(path, libraryVersion)

fun UnresolvedLibrary(path: String, libraryVersion: String?, lenient: Boolean): UnresolvedLibrary =
    if (lenient) LenientUnresolvedLibrary(path, libraryVersion) else RequiredUnresolvedLibrary(path, libraryVersion)

sealed class UnresolvedLibrary {
    abstract konst path: String
    abstract konst libraryVersion: String?
    abstract fun substitutePath(newPath: String): UnresolvedLibrary
}

data class RequiredUnresolvedLibrary(
    override konst path: String,
    override konst libraryVersion: String?
) : UnresolvedLibrary() {
    override fun substitutePath(newPath: String): RequiredUnresolvedLibrary {
        return copy(path = newPath)
    }
}

data class LenientUnresolvedLibrary(
    override konst path: String,
    override konst libraryVersion: String?
) : UnresolvedLibrary() {
    override fun substitutePath(newPath: String): LenientUnresolvedLibrary {
        return copy(path = newPath)
    }
}
