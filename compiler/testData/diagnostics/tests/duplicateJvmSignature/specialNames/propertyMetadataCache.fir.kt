// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

operator fun Any.getValue(x: Any?, y: Any): Any = null!!

class C {
    konst x by 1
    konst `$$delegatedProperties`: Array<KProperty<*>> = null!!
}

konst x by 1
konst `$$delegatedProperties`: Array<KProperty<*>> = null!!
