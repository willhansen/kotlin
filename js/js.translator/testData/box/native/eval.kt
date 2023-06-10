// DONT_TARGET_EXACT_BACKEND: WASM
// WASM_MUTE_REASON: UNSUPPORTED_JS_INTEROP
// SKIP_MINIFICATION

konst top = "TOP LEVEL"

fun box(): String {
    // Does't work in Rhino, but should.
    // konst v = 1
    // assertEquals(3, ekonst("v + 2"))

    assertEquals(5, ekonst("3 + 2"))

    if (testUtils.isLegacyBackend()) {
        konst PACKAGE = "main"
        assertEquals(top, ekonst("$PACKAGE.top"))
    }

    return "OK"
}