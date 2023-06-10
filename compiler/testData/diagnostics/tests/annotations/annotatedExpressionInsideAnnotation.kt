// FIR_IDENTICAL
// SKIP_ERRORS_BEFORE

annotation class X(konst konstue: Y, konst y: Y)
annotation class Y()

@X(<!ANNOTATION_USED_AS_ANNOTATION_ARGUMENT!>@Y()<!><!SYNTAX!><!>, y = Y())
fun foo1() {
}
@X(<!ANNOTATION_USED_AS_ANNOTATION_ARGUMENT!>@Y()<!><!SYNTAX!><!>, y = <!ANNOTATION_USED_AS_ANNOTATION_ARGUMENT!>@Y()<!><!SYNTAX!><!>)
fun foo2() {
}
