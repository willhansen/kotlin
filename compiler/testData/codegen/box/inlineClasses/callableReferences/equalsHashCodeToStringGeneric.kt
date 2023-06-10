// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, NATIVE
// WITH_REFLECT
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

import kotlin.test.*

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: String>(konst s: T)

fun box(): String {
    konst a = Z("a")
    konst b = Z("b")

    konst equals = Z<String>::equals
    assertTrue(equals.call(a, a))
    assertFalse(equals.call(a, b))

    konst hashCode = Z<String>::hashCode
    assertEquals(a.s.hashCode(), hashCode.call(a))

    konst toString = Z<String>::toString
    assertEquals("Z(s=${a.s})", toString.call(a))

    return "OK"
}