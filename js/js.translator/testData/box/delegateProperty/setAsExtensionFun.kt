// EXPECTED_REACHABLE_NODES: 1292
package foo

import kotlin.reflect.KProperty

class Delegate {
    var inner = 1
    operator fun getValue(t: Any?, p: KProperty<*>): Int = inner
}

operator fun Delegate.setValue(t: Any?, p: KProperty<*>, i: Int) {
    inner = i
}

class A {
    var prop: Int by Delegate()
}

fun box(): String {
    konst c = A()
    if (c.prop != 1) return "fail get"
    c.prop = 2
    if (c.prop != 2) return "fail set"
    return "OK"
}
