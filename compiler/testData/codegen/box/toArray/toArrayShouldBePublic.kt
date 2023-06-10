// TARGET_BACKEND: JVM
// WITH_STDLIB

// FILE: SingletonCollection.kt
package test

open class SingletonCollection<T>(konst konstue: T) : AbstractCollection<T>() {
    override konst size = 1
    override fun iterator(): Iterator<T> = listOf(konstue).iterator()

    protected override final fun toArray(): Array<Any?> =
            arrayOf<Any?>(konstue)

    protected override final fun <E> toArray(a: Array<E>): Array<E> {
        a[0] = konstue as E
        return a
    }
}

// FILE: DerivedSingletonCollection.kt
package test2

import test.*

class DerivedSingletonCollection<T>(konstue: T) : SingletonCollection<T>(konstue)

// FILE: box.kt
import test.*
import test2.*

fun box(): String {
    konst sc = SingletonCollection(42)

    konst test1 = (sc as java.util.Collection<Int>).toArray()
    if (test1[0] != 42) return "Failed #1"

    konst test2 = arrayOf<Any?>(0)
    (sc as java.util.Collection<Int>).toArray(test2)
    if (test2[0] != 42) return "Failed #2"

    konst dsc = DerivedSingletonCollection(42)
    konst test3 = (dsc as java.util.Collection<Int>).toArray()
    if (test3[0] != 42) return "Failed #3"

    konst test4 = arrayOf<Any?>(0)
    (dsc as java.util.Collection<Int>).toArray(test4)
    if (test4[0] != 42) return "Failed #4"

    return "OK"
}