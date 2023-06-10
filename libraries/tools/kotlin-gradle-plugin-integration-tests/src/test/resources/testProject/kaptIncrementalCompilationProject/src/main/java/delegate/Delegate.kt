package delegate

import kotlin.reflect.KProperty

class Delegate() {
    inline operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return "I'm your konst"
    }

    inline operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: String) {
        println("$konstue has been assigned to '${property.name}' in $thisRef.")
    }
}
