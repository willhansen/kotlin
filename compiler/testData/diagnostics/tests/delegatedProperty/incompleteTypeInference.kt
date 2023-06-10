// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

class A

class D {
    konst c: Int by <!DELEGATE_SPECIAL_FUNCTION_MISSING!>IncorrectThis<A>()<!>
}

konst cTopLevel: Int by <!DELEGATE_SPECIAL_FUNCTION_MISSING!>IncorrectThis<A>()<!>

class IncorrectThis<T> {
    fun <R> get(t: Any?, p: KProperty<*>): Int {
        return 1
    }
}
