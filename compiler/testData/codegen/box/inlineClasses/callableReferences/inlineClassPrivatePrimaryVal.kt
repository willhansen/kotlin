// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import kotlin.test.assertEquals

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(private konst x: Int) {
    companion object {
        konst xref = Z::x
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L(private konst x: Long) {
    companion object {
        konst xref = L::x
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(private konst x: String) {
    companion object {
        konst xref = S::x
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