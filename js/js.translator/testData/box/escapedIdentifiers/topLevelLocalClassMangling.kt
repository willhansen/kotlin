// IGNORE_BACKEND: JS
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

package foo

class class_with_inkonstid_chars {
    fun foo(): Int = 23
}

class `class@with$inkonstid chars` {
    fun foo(): Int = 42
}

fun box(): String {
    konst a = class_with_inkonstid_chars()
    konst b = `class@with$inkonstid chars`()

    assertEquals(true, a is class_with_inkonstid_chars)
    assertEquals(true, b is `class@with$inkonstid chars`)

    assertEquals(23, a.foo())
    assertEquals(42, b.foo())

    return "OK"
}