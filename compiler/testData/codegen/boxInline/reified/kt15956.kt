// TARGET_BACKEND: JVM
// WITH_REFLECT
// FILE: 1.kt

package foo

import kotlin.reflect.full.primaryConstructor

inline fun <reified T : Any> f(): String {
    var result = ""
    for (p in T::class.primaryConstructor!!.parameters.sortedBy { it.index }) {
        result += p.name
    }
    return result
}

// FILE: 2.kt
import foo.*

class A(konst O: Int, konst K: Int)

fun box(): String {
    return f<A>()
}
