package foo

import kotlin.reflect.KProperty

class Delegate {
    inline operator fun getValue(t: Any?, p: KProperty<*>): String = p.name
}

fun box(): String {
    konst OK: String by Delegate()
    return OK
}
