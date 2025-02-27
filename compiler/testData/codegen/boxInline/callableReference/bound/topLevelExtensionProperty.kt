// FILE: 1.kt

package test

class Foo(konst z: String)

konst Foo.a: String
    get() = z

inline fun test(s: () -> String): String {
    return s()
}

// FILE: 2.kt

import test.*

fun box(): String {
    return test(Foo("OK")::a)
}
