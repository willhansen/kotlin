// TARGET_BACKEND: JVM
// WITH_STDLIB

package test

import kotlin.reflect.typeOf
import kotlin.reflect.KTypeParameter
import kotlin.test.assertEquals

class Container<T>

class B<W>

class C<X> {
    konst <Y> B<Y>.createY: KTypeParameter where Y : X
        get() = typeOf<Container<Y>>().arguments.single().type!!.classifier as KTypeParameter
}

fun box(): String {
    with(C<Any>()) {
        konst y = B<Any>().createY
        assertEquals("X (Kotlin reflection is not available)", y.upperBounds.joinToString())
    }
    return "OK"
}
