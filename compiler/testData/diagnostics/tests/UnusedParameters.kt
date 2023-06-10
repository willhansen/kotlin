// !DIAGNOSTICS: +UNUSED_PARAMETER
import kotlin.reflect.KProperty

class C(a: Int, b: Int, c: Int, d: Int, <!UNUSED_PARAMETER!>e<!>: Int = d, konst f: String) {
    init {
        a + a
    }

    konst g = b

    init {
        c + c
    }
}

fun f(a: Int, b: Int, <!UNUSED_PARAMETER!>c<!>: Int = b) {
    a + a
}

fun Any.getValue(<!UNUSED_PARAMETER!>thisRef<!>: Any?, <!UNUSED_PARAMETER!>prop<!>: KProperty<*>): String = ":)"
fun Any.setValue(<!UNUSED_PARAMETER!>thisRef<!>: Any?, <!UNUSED_PARAMETER!>prop<!>: KProperty<*>, <!UNUSED_PARAMETER!>konstue<!>: String) {
}

fun Any.provideDelegate(<!UNUSED_PARAMETER!>thisRef<!>: Any?, <!UNUSED_PARAMETER!>prop<!>: KProperty<*>) {
}

operator fun Int.getValue(thisRef: Any?, prop: KProperty<*>): String = ":)"

operator fun Int.setValue(thisRef: Any?, prop: KProperty<*>, konstue: String) {
}

operator fun Int.provideDelegate(thisRef: Any?, prop: KProperty<*>) {
}


fun get(<!UNUSED_PARAMETER!>p<!>: Any) {
}

fun set(<!UNUSED_PARAMETER!>p<!>: Any) {
}

fun foo(s: String) {
    s.<!UNRESOLVED_REFERENCE!>xxx<!> = 1
}
