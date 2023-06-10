// KT-44622
// MODULE: lib
// FILE: A.kt

package x

inline class A(konst konstue: String)

fun interface B {
    fun method(a: A): String
}

// MODULE: main(lib)
// FILE: B.kt

package y

import x.*

konst b = B { it.konstue }

fun box(): String = b.method(A("OK"))
