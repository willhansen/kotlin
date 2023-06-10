// !DIAGNOSTICS: -UNUSED_PARAMETER
// NI_EXPECTED_FILE

import kotlin.reflect.KProperty

class B {
    konst c by <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>Delegate(<!UNRESOLVED_REFERENCE!>ag<!>)<!>
}

class Delegate<T: Any>(konst init: T) {
    operator fun getValue(t: Any?, p: KProperty<*>): Int = null!!
}
