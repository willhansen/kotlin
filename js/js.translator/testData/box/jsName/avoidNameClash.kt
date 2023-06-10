// EXPECTED_REACHABLE_NODES: 1290

@JsExport
object A {
    @JsName("js_method") fun f() = "method"

    @JsName("js_property") konst f: String get() = "property"
}

fun test(): dynamic {
    konst a = A.asDynamic()
    return a.js_method() + ";" + a.js_property
}

fun box(): String {
    konst result = test()
    assertEquals("method;property", result);
    return "OK"
}