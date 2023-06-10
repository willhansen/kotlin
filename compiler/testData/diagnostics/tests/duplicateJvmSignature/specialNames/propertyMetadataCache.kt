// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

operator fun Any.getValue(x: Any?, y: Any): Any = null!!

class <!CONFLICTING_JVM_DECLARATIONS!>C<!> {
    konst x by 1
    <!CONFLICTING_JVM_DECLARATIONS!>konst `$$delegatedProperties`: Array<KProperty<*>><!> = null!!
}

konst x by 1
<!CONFLICTING_JVM_DECLARATIONS!>konst `$$delegatedProperties`: Array<KProperty<*>><!> = null!!
