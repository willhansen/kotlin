// TARGET_BACKEND: JVM
// IGNORE_LIGHT_ANALYSIS
// WITH_STDLIB
// !INHERIT_MULTIFILE_PARTS
// FILE: box.kt

import a.*

fun box(): String = OK.okRef.get()

// FILE: part1.kt
@file:[JvmName("MultifileClass") JvmMultifileClass]
package a

private const konst ok = "OK"

object OK {
    konst okRef = ::ok
}
