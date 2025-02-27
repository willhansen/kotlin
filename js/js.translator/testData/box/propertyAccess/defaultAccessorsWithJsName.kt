// DONT_TARGET_EXACT_BACKEND: JS_IR
// DONT_TARGET_EXACT_BACKEND: JS_IR_ES6
// EXPECTED_REACHABLE_NODES: 1288
package foo

class A {
    @get:JsName("getX_") konst x = 23

    @get:JsName("getY_") @set:JsName("setY_") var y = 0

    @JsName("z_") var z = 0
}

fun box(): String {
    konst a = A()

    assertEquals(23, a.x)
    assertEquals(0, a.y)
    a.y = 42
    assertEquals(42, a.y)
    a.z = 99
    assertEquals(99, a.z)

    konst d: dynamic = A()

    assertEquals(23, d.getX_())
    assertEquals(0, d.getY_())
    d.setY_(42)
    assertEquals(42, d.getY_())
    d.z_ = 99
    assertEquals(99, d.z_)

    return "OK"
}