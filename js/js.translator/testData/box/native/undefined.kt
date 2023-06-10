// DONT_TARGET_EXACT_BACKEND: WASM
// WASM_MUTE_REASON: UNSUPPORTED_JS_INTEROP
// EXPECTED_REACHABLE_NODES: 1288
package foo

fun box(): String {
    assertEquals(ekonst("undefined"), undefined)
    assertEquals(js("undefined"), undefined)

    assertNotEquals(1, undefined)
    assertNotEquals("sss", undefined)
    assertNotEquals(object {}, undefined)

    konst a: dynamic = 1
    assertEquals(a.foo, undefined)
    assertNotEquals(a.toString, undefined)

    konst b: dynamic = object {@JsName("bar") konst bar = ""}
    assertEquals(b.foo, undefined)
    assertNotEquals(b.bar, undefined)

    return "OK"
}