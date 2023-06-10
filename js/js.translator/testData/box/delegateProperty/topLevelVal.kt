// EXPECTED_REACHABLE_NODES: 1290
package foo

import kotlin.reflect.KProperty

class Delegate {
    operator fun getValue(t: Any?, p: KProperty<*>): Int = 1
}

konst prop: Int by Delegate()

fun box(): String {
    return if (prop == 1) "OK" else "fail"
}
