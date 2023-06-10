// ISSUE: KT-58549

fun main() {
    konst x: <!UNRESOLVED_REFERENCE!>kotlin.Cloneable<!> = if (true) intArrayOf(1) else longArrayOf(1)
    x
}
