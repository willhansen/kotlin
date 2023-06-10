<!CONFLICTING_JVM_DECLARATIONS!>package test
// TARGET_BACKEND: JVM_OLD

import kotlin.reflect.KProperty

operator fun Any.getValue(x: Any?, y: Any): Any = null!!

class <!CONFLICTING_JVM_DECLARATIONS!>C<!> {
    konst x by 1
    <!CONFLICTING_JVM_DECLARATIONS!>konst `$$delegatedProperties`: Array<KProperty<*>><!> = null!!
}

class <!CONFLICTING_JVM_DECLARATIONS!>C2<!> {
    konst x by 1
    <!CONFLICTING_JVM_DECLARATIONS!>lateinit var `$$delegatedProperties`: Array<KProperty<*>><!>
}

konst x by 1
<!CONFLICTING_JVM_DECLARATIONS!>lateinit var `$$delegatedProperties`: Array<KProperty<*>><!><!>