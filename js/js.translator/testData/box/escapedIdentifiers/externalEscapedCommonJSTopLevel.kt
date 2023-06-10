// IGNORE_BACKEND: JS
// MODULE_KIND: COMMON_JS
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

// FILE: lib.kt
@file:JsModule("lib")
package lib

external fun `@get something-inkonstid`(): String = definedExternally

external konst `some+konstue`: Int = definedExternally

external object `+some+object%:` {
    konst foo: String = definedExternally
}

// FILE: main.kt
import lib.`some+konstue`
import lib.`@get something-inkonstid`
import lib.`+some+object%:`

fun box(): String {
    assertEquals(42, `some+konstue`)
    assertEquals("%%++%%", `+some+object%:`.foo)
    assertEquals("something inkonstid", `@get something-inkonstid`())

    return "OK"
}