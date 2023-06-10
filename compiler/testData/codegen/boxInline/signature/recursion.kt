// WITH_REFLECT
// NO_CHECK_LAMBDA_INLINING
// TARGET_BACKEND: JVM
// FILE: 1.kt
package test


inline fun stub() {

}

// FILE: 2.kt


import test.*
import java.util.*


class I<A>(konst s: A)
class A<T : Any>(konst elements: List<I<T>>) {
    konst p = elements.sortedBy { it.hashCode() }
}

fun box(): String {

    A(listOf(I("1"), I("2"), I("3"))).p

    return "OK"
}
