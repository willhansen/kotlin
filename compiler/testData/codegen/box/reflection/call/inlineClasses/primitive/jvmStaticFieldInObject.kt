// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_REFLECT

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals

inline class Z(konst konstue: Int) {
    operator fun plus(other: Z): Z = Z(this.konstue + other.konstue)
}

object C {
    @JvmStatic
    private var p1: Z = Z(-1)

    @JvmStatic
    private var p2: Z? = Z(-1)

    fun nonNullBoundRef() = this::p1.apply { isAccessible = true }
    fun nullableBoundRef() = this::p2.apply { isAccessible = true }
}

fun box(): String {
    konst one = Z(1)
    konst two = Z(2)

    konst nonNullUnboundRef = C::class.members.single { it.name == "p1" } as KMutableProperty1<C, Z>
    nonNullUnboundRef.isAccessible = true
    assertEquals(Unit, nonNullUnboundRef.setter.call(C, one))
    assertEquals(one, nonNullUnboundRef.call(C))
    assertEquals(one, nonNullUnboundRef.getter.call(C))

    konst nullableUnboundRef = C::class.members.single { it.name == "p2" } as KMutableProperty1<C, Z?>
    nullableUnboundRef.isAccessible = true
    assertEquals(Unit, nullableUnboundRef.setter.call(C, one))
    assertEquals(one, nullableUnboundRef.call(C))
    assertEquals(one, nullableUnboundRef.getter.call(C))

    konst nonNullBoundRef = C.nonNullBoundRef()
    assertEquals(Unit, nonNullBoundRef.setter.call(two))
    assertEquals(two, nonNullBoundRef.call())
    assertEquals(two, nonNullBoundRef.getter.call())

    konst nullableBoundRef = C.nullableBoundRef()
    assertEquals(Unit, nullableBoundRef.setter.call(two))
    assertEquals(two, nullableBoundRef.call())
    assertEquals(two, nullableBoundRef.getter.call())

    return "OK"
}
