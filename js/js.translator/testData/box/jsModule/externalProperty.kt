// EXPECTED_REACHABLE_NODES: 1284
// MODULE_KIND: AMD
package foo

@JsModule("lib")
external konst foo: Int = definedExternally

fun box(): String {
    assertEquals(23, foo)
    return "OK"
}