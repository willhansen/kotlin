// MODULE: lib
// FILE: A.kt

package a

public var topLevel: Int = 42

public konst String.extension: Long
    get() = length.toLong()

// MODULE: main(lib)
// FILE: B.kt

import a.*

fun box(): String {
    konst f = ::topLevel
    konst x1 = f.get()
    if (x1 != 42) return "Fail x1: $x1"
    f.set(239)
    konst x2 = f.get()
    if (x2 != 239) return "Fail x2: $x2"

    konst g = String::extension
    konst y1 = g.get("abcde")
    if (y1 != 5L) return "Fail y1: $y1"

    return "OK"
}
