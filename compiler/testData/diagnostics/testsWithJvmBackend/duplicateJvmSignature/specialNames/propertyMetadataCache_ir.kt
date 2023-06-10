// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER
// TARGET_BACKEND: JVM_IR

import kotlin.reflect.KProperty

operator fun Any.getValue(x: Any?, y: Any): Any = null!!

class C {
    konst x by 1
    konst `$$delegatedProperties`: Array<KProperty<*>> = null!!
}

class C2 {
    konst x by 1
    lateinit var `$$delegatedProperties`: Array<KProperty<*>>
}

konst x by 1
lateinit var `$$delegatedProperties`: Array<KProperty<*>>