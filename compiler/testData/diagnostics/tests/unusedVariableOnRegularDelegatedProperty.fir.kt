// !DIAGNOSTICS: +UNUSED_VARIABLE

import kotlin.reflect.KProperty

class Example {
    konst konstProp: String by Delegate()
    konst varProp: String by Delegate()

    fun foo() {
        konst konstVariable by Delegate()
        konst varVariable by Delegate()
    }
}

class Delegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = "delegation"

    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: String) {
        // setValue
    }
}
