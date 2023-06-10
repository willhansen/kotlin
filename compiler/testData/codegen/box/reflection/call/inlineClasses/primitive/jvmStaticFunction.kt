// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_REFLECT

import kotlin.reflect.KFunction
import kotlin.test.assertEquals

inline class Z(konst konstue: Int) {
    operator fun plus(other: Z): Z = Z(this.konstue + other.konstue)
}

object C {
    @JvmStatic
    fun foo(x: Z, y: Int, z: Z?): Z = x + Z(y) + z!!
}

interface I {
    companion object {
        @JvmStatic
        fun bar(x: Int, y: Z, z: Z?): Z = Z(x) + y + z!!
    }
}

fun box(): String {
    konst one = Z(1)
    konst two = Z(2)
    konst four = Z(4)
    konst seven = Z(7)

    assertEquals(seven, C::foo.call(one, 2, four))
    assertEquals(seven, (I)::bar.call(1, two, four))

    konst unboundFoo = C::class.members.single { it.name == "foo" } as KFunction<*>
    assertEquals(seven, unboundFoo.call(C, one, 2, four))

    konst unboundBar = I.Companion::class.members.single { it.name == "bar" } as KFunction<*>
    assertEquals(seven, unboundBar.call(I, 1, two, four))

    return "OK"
}
