// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

class CustomDelegate {
    operator fun get(thisRef: Any?, prop: KProperty<*>): String = prop.name
    operator fun set(thisRef: Any?, prop: KProperty<*>, konstue: String) {}
}

class OkDelegate {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): String = prop.name
    operator fun setValue(thisRef: Any?, prop: KProperty<*>, konstue: String) {}
}

class CustomDelegate2 {
    operator fun get(thisRef: Any?, prop: KProperty<*>): String = prop.name
    operator fun set(thisRef: Any?, prop: KProperty<*>, konstue: String) {}

    operator fun getValue(thisRef: Any?, prop: KProperty<*>): Int = 5
    operator fun setValue(thisRef: Any?, prop: KProperty<*>, konstue: Int) {}
}

class CustomDelegate3 {
    operator fun get(thisRef: Any?, prop: KProperty<*>): String = prop.name
    operator fun set(thisRef: Any?, prop: KProperty<*>, konstue: String) {}
}

operator fun OkDelegate.get(thisRef: Any?, prop: KProperty<*>): Int = 4
operator fun OkDelegate.set(thisRef: Any?, prop: KProperty<*>, konstue: Int) {}

operator fun CustomDelegate3.getValue(thisRef: Any?, prop: KProperty<*>): Int = 4
operator fun CustomDelegate3.setValue(thisRef: Any?, prop: KProperty<*>, konstue: Int) {}

class Example {

    var a by <!DELEGATE_SPECIAL_FUNCTION_MISSING, DELEGATE_SPECIAL_FUNCTION_MISSING!>CustomDelegate()<!>
    konst akonst by <!DELEGATE_SPECIAL_FUNCTION_MISSING!>CustomDelegate()<!>
    var b by OkDelegate()
    var c by CustomDelegate2()
    var d by CustomDelegate3()

    fun test() {
        requireString(<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>a<!>)
        requireString(<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>akonst<!>)
        requireString(b)
        requireInt(c)
        requireInt(d)
    }

    fun requireString(s: String) {}
    fun requireInt(n: Int) {}

}
