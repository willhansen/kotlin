// FIR_IDENTICAL
@Target(AnnotationTarget.CLASS)
annotation class My
data class Pair(konst a: Int, konst b: Int)
fun foo(): Int {
    konst (<!WRONG_ANNOTATION_TARGET!>@My<!> <!WRONG_MODIFIER_TARGET!>private<!> a, <!WRONG_ANNOTATION_TARGET!>@My<!> <!WRONG_MODIFIER_TARGET!>public<!> b) = Pair(12, 34)
    return a + b
}