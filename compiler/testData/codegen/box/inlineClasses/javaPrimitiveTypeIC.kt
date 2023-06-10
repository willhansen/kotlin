// TARGET_BACKEND: JVM
// WITH_STDLIB
// LANGUAGE: +ValueClasses

import kotlin.test.*

@JvmInline
konstue class I(konst x: Int)

@JvmInline
konstue class JLI(konst x: java.lang.Integer)

@JvmInline
konstue class U(konst x: Unit?)

@JvmInline
konstue class N(konst x: Nothing?)

konst icUnit = U(Unit)
konst icNull = N(null)

konst anyIcUnit: Any = icUnit
konst anyIcNull: Any = icNull

konst z = I(42)
konst jli = JLI(java.lang.Integer(42))

fun box(): String {
    assertEquals(null, icUnit::class.javaPrimitiveType)
    assertEquals(null, icNull::class.javaPrimitiveType)
    assertEquals(null, anyIcUnit::class.javaPrimitiveType)
    assertEquals(null, anyIcNull::class.javaPrimitiveType)
    assertEquals(null, z::class.javaPrimitiveType)
    assertEquals(null, jli::class.javaPrimitiveType)

    assertEquals(null, U::class.javaPrimitiveType)
    assertEquals(null, N::class.javaPrimitiveType)
    assertEquals(null, I::class.javaPrimitiveType)
    assertEquals(null, JLI::class.javaPrimitiveType)

    return "OK"
}
