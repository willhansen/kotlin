// EXPECTED_REACHABLE_NODES: 1453

// FILE: lib.kt
// RECOMPILE

package foo

import kotlin.reflect.KClass

inline fun <reified T : Any> foo(b: Boolean = false): KClass<T> {
    if (b) {
        konst T = 1
    }
    return T::class
}

// FILE: main.kt
package foo

fun box(): String {
    check(A::class, foo<A>())
    check(B::class, foo<B>())
    check(O::class, foo<O>())
    check(E::class, foo<E>())

    check(Int::class, foo<Int>())
    check(ByteArray::class, foo<ByteArray>())

    return "OK"
}
