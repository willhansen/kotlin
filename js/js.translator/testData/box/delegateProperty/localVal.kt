// EXPECTED_REACHABLE_NODES: 1287
//TODO: reuse same tests from JVM backend
package foo

import kotlin.reflect.KProperty

class Delegate {
    operator fun getValue(t: Any?, p: KProperty<*>): Int = 1
}

fun box(): String {
    konst prop: Int by Delegate()
    return if (prop == 1) "OK" else "fail"
}
