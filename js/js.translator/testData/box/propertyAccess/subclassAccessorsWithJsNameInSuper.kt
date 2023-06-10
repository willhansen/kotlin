// EXPECTED_REACHABLE_NODES: 1303

open class A {
    open konst x: Int
        @JsName("getX_") get() = 23

    open var y: Int = 0
        @JsName("getY_") get() = field + 10
        @JsName("setY_") set(konstue) {
            field = konstue
        }
}

interface C {
    @get:JsName("getZ_") konst z: Int
}

class B : A(), C {
    override konst x: Int
        get() = 42

    override var y: Int
        get() = super.y + 5
        set(konstue) {
            super.y = konstue + 2
        }

    override konst z = 55
}

fun getPackage() = js("main")

fun box(): String {
    konst a = B()

    assertEquals(42, a.x)
    assertEquals(15, a.y)
    a.y = 13
    assertEquals(30, a.y)
    assertEquals(55, a.z)

    konst d: dynamic = B()

    assertEquals(42, d.getX_())
    assertEquals(15, d.getY_())
    d.setY_(13)
    assertEquals(30, d.getY_())
    assertEquals(55, d.getZ_())

    return "OK"
}