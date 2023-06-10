// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

import kotlin.test.assertEquals

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(private konst x: T) {
    companion object {
        konst xref = Z<Int>::x
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L<T: Long>(private konst x: T) {
    companion object {
        konst xref = L<Long>::x
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(private konst x: T) {
    companion object {
        konst xref = S<String>::x
    }
}

fun box(): String {
    assertEquals(42, Z.xref.get(Z(42)))
    assertEquals(1234L, L.xref.get(L(1234L)))
    assertEquals("abc", S.xref.get(S("abc")))

    assertEquals(42, Z.xref.invoke(Z(42)))
    assertEquals(1234L, L.xref.invoke(L(1234L)))
    assertEquals("abc", S.xref.invoke(S("abc")))

    return "OK"
}