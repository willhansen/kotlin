// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// LANGUAGE: +ValueClasses

import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals

@JvmInline
konstue class S(konst konstue1: UInt, konst konstue2: Int) {
    operator fun plus(other: S): S = S(this.konstue1 + other.konstue1, this.konstue2 + other.konstue2)
}

class C {
    private var nonNullMember: S = S(UInt.MAX_VALUE, -10)
    private var nullableMember: S? = S(UInt.MAX_VALUE, -10)

    fun nonNullUnboundRef() = C::nonNullMember.apply { isAccessible = true }
    fun nonNullBoundRef() = this::nonNullMember.apply { isAccessible = true }
    fun nullableUnboundRef() = C::nullableMember.apply { isAccessible = true }
    fun nullableBoundRef() = this::nullableMember.apply { isAccessible = true }
}

private var nonNullTopLevel: S = S(UInt.MAX_VALUE, -10)
private var nullableTopLevel: S? = S(UInt.MAX_VALUE, -10)

fun box(): String {
    konst c = C()
    konst zero = S(0U, 5)
    konst one = S(1U, 10)
    konst two = S(2U, 20)

    assertEquals(Unit, c.nonNullUnboundRef().setter.call(c, zero))
    assertEquals(zero, c.nonNullUnboundRef().call(c))
    assertEquals(zero, c.nonNullUnboundRef().getter.call(c))

    assertEquals(Unit, c.nonNullBoundRef().setter.call(one))
    assertEquals(one, c.nonNullBoundRef().call())
    assertEquals(one, c.nonNullBoundRef().getter.call())

    assertEquals(Unit, c.nullableUnboundRef().setter.call(c, zero))
    assertEquals(zero, c.nullableUnboundRef().call(c))
    assertEquals(zero, c.nullableUnboundRef().getter.call(c))

    assertEquals(Unit, c.nullableBoundRef().setter.call(one))
    assertEquals(one, c.nullableBoundRef().call())
    assertEquals(one, c.nullableBoundRef().getter.call())

    konst nonNullTopLevel = ::nonNullTopLevel.apply { isAccessible = true }
    assertEquals(Unit, nonNullTopLevel.setter.call(two))
    assertEquals(two, nonNullTopLevel.call())
    assertEquals(two, nonNullTopLevel.getter.call())

    konst nullableTopLevel = ::nullableTopLevel.apply { isAccessible = true }
    assertEquals(Unit, nullableTopLevel.setter.call(two))
    assertEquals(two, nullableTopLevel.call())
    assertEquals(two, nullableTopLevel.getter.call())

    return "OK"
}
