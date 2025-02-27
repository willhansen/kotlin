// TARGET_BACKEND: JVM
// IGNORE_LIGHT_ANALYSIS
// WITH_STDLIB
// !INHERIT_MULTIFILE_PARTS
// FILE: box.kt

import a.funRefA
import b.funRefB

fun box(): String {
    if (funRefA != funRefB) return "Fail: funRefA != funRefB"
    return "OK"
}

// FILE: a.kt

package a

import test.function

konst funRefA = ::function

// FILE: b.kt

package b

import test.function

konst funRefB = ::function

// FILE: part.kt

@file:[JvmName("MultifileClass") JvmMultifileClass]
package test

fun function() {}
