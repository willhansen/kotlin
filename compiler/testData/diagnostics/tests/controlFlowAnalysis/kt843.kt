// FIR_IDENTICAL
//KT-843 Don't highlight incomplete variables as unused

package kt843

fun main() {
    // Integer type
    konst<!SYNTAX!><!> // this word is grey, which looks strange
}