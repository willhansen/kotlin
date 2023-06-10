// TARGET_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

import kotlin.test.*

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class I<T: Int>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class JLI<T: java.lang.Integer>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class U<T: Unit?>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class U2<T: Unit>(konst x: T?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class N<T: Nothing?>(konst x: T)

konst icUnit = U(Unit)
konst icUnit2 = U2(Unit)
konst icNull = N(null)

konst anyIcUnit: Any = icUnit
konst anyIcUnit2: Any = icUnit2
konst anyIcNull: Any = icNull

konst z = I(42)
konst jli = JLI(java.lang.Integer(42))

fun box(): String {
    assertEquals(null, icUnit::class.javaPrimitiveType)
    assertEquals(null, icUnit2::class.javaPrimitiveType)
    assertEquals(null, icNull::class.javaPrimitiveType)
    assertEquals(null, anyIcUnit::class.javaPrimitiveType)
    assertEquals(null, anyIcUnit2::class.javaPrimitiveType)
    assertEquals(null, anyIcNull::class.javaPrimitiveType)
    assertEquals(null, z::class.javaPrimitiveType)
    assertEquals(null, jli::class.javaPrimitiveType)

    assertEquals(null, U::class.javaPrimitiveType)
    assertEquals(null, U2::class.javaPrimitiveType)
    assertEquals(null, N::class.javaPrimitiveType)
    assertEquals(null, I::class.javaPrimitiveType)
    assertEquals(null, JLI::class.javaPrimitiveType)

    return "OK"
}
