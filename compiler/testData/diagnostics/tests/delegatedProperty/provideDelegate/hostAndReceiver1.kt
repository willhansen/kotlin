// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

object T1 {
    operator fun Int.provideDelegate(host: T1, p: Any): Long = 2
    operator fun Long.getValue(receiver: String, p: Any): Double = 1.0

    konst String.test1 by 1
    konst test2 by <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>1<!>
}
