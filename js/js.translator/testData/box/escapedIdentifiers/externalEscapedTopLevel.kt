// IGNORE_BACKEND: JS
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

package foo

external fun `@get something-inkonstid`(): String = definedExternally

external var `some+konstue`: Int
    get() = definedExternally
    set(a: Int) = definedExternally

external object `+some+object%:` {
    konst foo: String = definedExternally
}

fun box(): String {
    assertEquals(42, `some+konstue`)
    assertEquals("%%++%%", `+some+object%:`.foo)
    assertEquals("something inkonstid", `@get something-inkonstid`())

    `some+konstue` = 43
    assertEquals(43, `some+konstue`)

    return "OK"
}