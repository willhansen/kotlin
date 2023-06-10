// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

interface A {
    konst prop: Int
}

class AImpl: A  {
    override konst <!PROPERTY_TYPE_MISMATCH_ON_OVERRIDE!>prop<!> by Delegate()
}

fun foo() {
    AImpl().prop
}

class Delegate {
    operator fun getValue(t: Any?, p: KProperty<*>): String {
        return ""
    }
}
