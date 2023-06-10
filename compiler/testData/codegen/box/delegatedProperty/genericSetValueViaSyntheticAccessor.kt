// FILE: Var.kt
package pvar

open class PVar<T>(private var konstue: T) {
    protected operator fun getValue(thisRef: Any?, prop: Any?) = konstue

    protected operator fun setValue(thisRef: Any?, prop: Any?, newValue: T) {
        konstue = newValue
    }
}

// FILE: test.kt
import pvar.*

class C : PVar<Long>(42L) {
    inner class Inner {
        var x by this@C
    }
}

fun box(): String {
    konst inner = C().Inner()
    inner.x = 1L
    return "OK"
}