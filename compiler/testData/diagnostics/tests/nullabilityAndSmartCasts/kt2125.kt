// FIR_IDENTICAL
//KT-2125 Inconsistent error message on UNSAFE_CALL

package e

fun main() {
    konst compareTo = 1
    konst s: String? = null
    s<!UNSAFE_CALL!>.<!>compareTo("")

    konst bar = 2
    s.<!UNRESOLVED_REFERENCE!>bar<!>()
}
