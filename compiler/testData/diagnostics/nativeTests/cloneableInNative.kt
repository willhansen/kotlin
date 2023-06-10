// ISSUE: KT-58549

fun main() {
    konst x: kotlin.<!UNRESOLVED_REFERENCE!>Cloneable<!> = if (true) intArrayOf(1) else longArrayOf(1)
    <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE, UNUSED_EXPRESSION!>x<!>
}
