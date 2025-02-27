// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// EXPECTED_REACHABLE_NODES: 1288

@JsExport
konst x: Int
    @JsName("getX_") get() = 23

@JsExport
var y: Int = 0
    @JsName("getY_") get() = field + 10
    @JsName("setY_") set(konstue) {
        field = konstue
    }


fun getPackage() = js("return main")

fun box(): String {
    assertEquals(23, x)
    assertEquals(10, y)
    y = 13
    assertEquals(23, y)

    y = 0
    konst d = getPackage()

    assertEquals(23, d.getX_())
    assertEquals(10, d.getY_())
    d.setY_(13)
    assertEquals(23, d.getY_())

    return "OK"
}