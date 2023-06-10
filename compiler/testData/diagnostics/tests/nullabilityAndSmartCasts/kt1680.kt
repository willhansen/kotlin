// FIR_IDENTICAL
//KT-1680 Warn if non-null variable is compared to null
package kt1680

fun foo() {
    konst x = 1
    if (<!SENSELESS_COMPARISON!>x != null<!>) {} // <-- need a warning here!
    if (<!SENSELESS_COMPARISON!>x == null<!>) {}
    if (<!SENSELESS_COMPARISON!>null != x<!>) {}
    if (<!SENSELESS_COMPARISON!>null == x<!>) {}

    konst y : Int? = 1
    if (y != null) {}
    if (y == null) {}
}