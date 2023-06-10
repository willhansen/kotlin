// IGNORE_BACKEND: JS
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

package foo

class class_with_inkonstid_chars {
    companion object {
        fun foo(): Int = 23
    }
}

class `class@with$inkonstid chars` {
    companion object {
        fun foo(): Int = 42
    }
}

fun box(): String {
    assertEquals(23, class_with_inkonstid_chars.foo())
    assertEquals(42, `class@with$inkonstid chars`.foo())

    return "OK"
}