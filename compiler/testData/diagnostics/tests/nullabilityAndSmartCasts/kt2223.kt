// FIR_IDENTICAL
//KT-2223 Comparing non-null konstue with null might produce helpful warning
package kt2223

fun foo() {
    konst x: Int? = null
    if (x == null) return
    if (<!SENSELESS_COMPARISON!>x == null<!>) return
}
