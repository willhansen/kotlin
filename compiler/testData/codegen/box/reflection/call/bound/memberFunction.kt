// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.*

class C(konst k: String) {
    fun foo(s: String) = s + k
}

fun box(): String =
        C("K")::foo.call("O")

