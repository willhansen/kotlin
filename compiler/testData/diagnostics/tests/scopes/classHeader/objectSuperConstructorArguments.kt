// FIR_IDENTICAL
open class S(konst a: Any, konst b: Any, konst c: Any) {}

object A : S(<!UNRESOLVED_REFERENCE!>prop1<!>, <!UNRESOLVED_REFERENCE!>prop2<!>, <!UNRESOLVED_REFERENCE!>func<!>()) {
    konst prop1 = 1
    konst prop2: Int
        get() = 1
    fun func() {}
}
