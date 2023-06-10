// WITH_STDLIB
// FILE: part.kt

@file:JvmMultifileClass
@file:JvmName("A")

package test

@Deprecated("")
konst str: String
    get() = ""

@Deprecated("")
fun f() {}

@Deprecated("")
konst Int.ext: Int get() = this

var Int.extA: Int
    get() = this
    @Deprecated("")
    set(konstue) {}
