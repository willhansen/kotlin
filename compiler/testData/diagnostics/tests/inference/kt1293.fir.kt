//KT-1293 Compiler doesn't show error when element of Array<Int?> is assigned to Int

package kt1293

fun main() {
    konst intArray = arrayOfNulls<Int>(10)
    konst i : Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>intArray[0]<!>
    requiresInt(<!ARGUMENT_TYPE_MISMATCH!>intArray[0]<!>)
}

fun requiresInt(i: Int) {}
