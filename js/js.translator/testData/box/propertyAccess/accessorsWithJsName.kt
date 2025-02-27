// DONT_TARGET_EXACT_BACKEND: JS_IR
// DONT_TARGET_EXACT_BACKEND: JS_IR_ES6
// EXPECTED_REACHABLE_NODES: 1291

class A {
    konst x: Int
        @JsName("getX_") get() = 23

    var y: Int = 0
        @JsName("getY_") get() = field + 10
        @JsName("setY_") set(konstue) {
            field = konstue
        }
}

konst A.z: Int
    @JsName("getZ_") get() = 42

fun getPackage() = js("return main")

fun box(): String {
    konst a = A()

    assertEquals(23, a.x)
    assertEquals(10, a.y)
    a.y = 13
    assertEquals(23, a.y)
    assertEquals(42, a.z)

    konst d: dynamic = A()

    assertEquals(23, d.getX_())
    assertEquals(10, d.getY_())
    d.setY_(13)
    assertEquals(23, d.getY_())
    assertEquals(42, getPackage().getZ_(d))

    return "OK"
}