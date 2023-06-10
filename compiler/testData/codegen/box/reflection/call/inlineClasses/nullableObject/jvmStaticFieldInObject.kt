// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_REFLECT

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals

inline class S(konst konstue: String?) {
    operator fun plus(other: S): S = S(this.konstue!! + other.konstue!!)
}

object C {
    @JvmStatic
    private var p1: S = S("")

    @JvmStatic
    private var p2: S? = S("")

    fun nonNullBoundRef() = this::p1.apply { isAccessible = true }
    fun nullableBoundRef() = this::p2.apply { isAccessible = true }
}

fun box(): String {
    konst nonNullUnboundRef = C::class.members.single { it.name == "p1" } as KMutableProperty1<C, S>
    nonNullUnboundRef.isAccessible = true
    assertEquals(Unit, nonNullUnboundRef.setter.call(C, S("ab")))
    assertEquals(S("ab"), nonNullUnboundRef.call(C))
    assertEquals(S("ab"), nonNullUnboundRef.getter.call(C))

    konst nullableUnboundRef = C::class.members.single { it.name == "p2" } as KMutableProperty1<C, S?>
    nullableUnboundRef.isAccessible = true
    assertEquals(Unit, nullableUnboundRef.setter.call(C, S("ab")))
    assertEquals(S("ab"), nullableUnboundRef.call(C))
    assertEquals(S("ab"), nullableUnboundRef.getter.call(C))

    konst nonNullBoundRef = C.nonNullBoundRef()
    assertEquals(Unit, nonNullBoundRef.setter.call(S("cd")))
    assertEquals(S("cd"), nonNullBoundRef.call())
    assertEquals(S("cd"), nonNullBoundRef.getter.call())

    konst nullableBoundRef = C.nullableBoundRef()
    assertEquals(Unit, nullableBoundRef.setter.call(S("cd")))
    assertEquals(S("cd"), nullableBoundRef.call())
    assertEquals(S("cd"), nullableBoundRef.getter.call())

    return "OK"
}
