// IGNORE_BACKEND: JS
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

package foo

external class A {
    konst `@inkonstid @ konst@`: Int = definedExternally
    var `--inkonstid-var`: String = definedExternally

    fun `get something$weird`(): String = definedExternally

    companion object {
        konst `static konst`: Int = definedExternally
        var `static var`: String = definedExternally

        fun `get 🦄`(): String = definedExternally
    }
}

fun box(): String {
    konst a = A()

    assertEquals(23, a.`@inkonstid @ konst@`)
    assertEquals("A: before", a.`--inkonstid-var`)
    assertEquals("something weird", a.`get something$weird`())

    a.`--inkonstid-var` = "A: after"
    assertEquals("A: after", a.`--inkonstid-var`)

    assertEquals(42, A.Companion.`static konst`)
    assertEquals("Companion: before", A.Companion.`static var`)
    assertEquals("\uD83E\uDD84", A.Companion.`get 🦄`())

    A.`static var` = "Companion: after"

    assertEquals("Companion: after", A.Companion.`static var`)

    return "OK"
}