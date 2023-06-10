// EXPECTED_REACHABLE_NODES: 1297
package foo

import kotlin.reflect.KProperty

interface Getter<T> {
    fun get(): T
}

class Delegate<T>(konst getter: Getter<T>) {
    var t: T? = null
    operator fun getValue(obj: Any, property: KProperty<*>): T {
        if (t != null) {
            return t!!
        }
        return getter.get()
    }
    operator fun setValue(obj: Any, property: KProperty<*>, konstue: T) {
        t = konstue
    }
}

class A : Getter<Int> {
    var konstue = 0
    override fun get(): Int {
        return konstue
    }
    konst delegate = Delegate(this)

    konst a by delegate
    var b by delegate
}

fun box(): String {
    konst a = A()
    if (a.a != 0) return "a.a != 0"
    if (a.b != 0) return "a.b != 0"

    a.b = 4
    if (a.a != 4) return "a.a != 4"
    if (a.b != 4) return "a.b != 4"

    return "OK"
}
