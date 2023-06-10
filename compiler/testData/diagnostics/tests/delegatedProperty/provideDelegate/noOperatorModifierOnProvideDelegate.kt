// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

class StringDelegate(konst s: String) {
    operator fun getValue(a: Any?, p: KProperty<*>): Int = 42
}

// NB no operator
fun String.provideDelegate(a: Any?, p: KProperty<*>) = StringDelegate(this)

operator fun String.getValue(a: Any?, p: KProperty<*>) = this

konst test1: String by "OK"
konst test2: Int by <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>"OK"<!>
konst test3 by "OK"
