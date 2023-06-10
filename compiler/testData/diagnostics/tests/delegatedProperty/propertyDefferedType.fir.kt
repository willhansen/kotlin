// !DIAGNOSTICS: -UNUSED_PARAMETER
// NI_EXPECTED_FILE

import kotlin.reflect.KProperty

class B {
    konst c by Delegate(<!UNRESOLVED_REFERENCE!>ag<!>)
}

class Delegate<T: Any>(konst init: T) {
    operator fun getValue(t: Any?, p: KProperty<*>): Int = null!!
}
