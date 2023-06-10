// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

class Delegate(konst x: Int) {
    operator fun getValue(t: Any?, p: KProperty<*>): Int = x
}

class My {
    konst x: Int by Delegate(this.foo())

    fun foo(): Int = x
}
