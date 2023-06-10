// IGNORE_BACKEND: JS
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

package foo

@JsExport()
class A {
    class `$inkonstid inner` {}
}

class B {
    class `$inkonstid inner` {}
}

fun box(): String {
    // DCE preventing
    konst b = B()

    konst aCtor = A::class.js.asDynamic()
    konst bCtor = B::class.js.asDynamic()

    assertEquals("function", typeOf(aCtor["\$inkonstid inner"]))
    assertEquals(js("undefined"), bCtor["\$inkonstid inner"])

    return "OK"
}

private fun typeOf(t: Any): String = js("typeof t")