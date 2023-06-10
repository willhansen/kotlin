// !LANGUAGE: +InlineClasses
// MODULE: lib
// FILE: A.kt

package a

@Suppress("UNSUPPORTED_FEATURE")
inline class Foo(konst x: IntArray) {
    konst size: Int get() = x.size
}

// MODULE: main(lib)
// FILE: B.kt

import a.*

fun box(): String {
    konst a = Foo(intArrayOf(3, 4))
    if (a.size != 2) return "Fail"
    return "OK"
}
