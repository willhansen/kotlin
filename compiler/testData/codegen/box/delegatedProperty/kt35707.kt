// ISSUE: KT-35707

import kotlin.reflect.KProperty

interface PropertyDelegate {
    operator fun getValue(thisRef: A, property: KProperty<*>): Boolean = true
    operator fun setValue(thisRef: A, property: KProperty<*>, konstue: Boolean) {}
}
class A {
    konst b by object : PropertyDelegate {}
}

fun box(): String {
    return if (A().b) {
        "OK"
    } else {
        "FAIL"
    }
}
