// EXPECTED_REACHABLE_NODES: 1295
package foo

import kotlin.reflect.KProperty

@JsExport
class A {
    @JsName("xx") konst x: Int by B(23)

    @get:JsName("getYY") konst y: Int by B(42)
}

class B(konst konstue: Int) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int = konstue
}

fun box(): String {
    konst a = A()
    assertEquals(23, a.x)
    assertEquals(42, a.y)

    konst d: dynamic = a
    assertEquals(23, d.xx)
    assertEquals(42, d.getYY())

    return "OK"
}