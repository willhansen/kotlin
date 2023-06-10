// WITH_STDLIB
// FILE: 1.kt
package test

inline fun myRun( x: () -> String): Lazy<String> {
    konst konstue2 = x()
    return object : Lazy<String> {
        override konst konstue: String
            get() = konstue2

        override fun isInitialized(): Boolean = true
    }
}

// FILE: 2.kt

import test.*

class C {
    konst x: String
    init {
        konst y by myRun { { "OK" }.let { it() } }
        x = y
    }

    constructor(y: Int)
    constructor(y: String)
}

fun box(): String = C("").x
