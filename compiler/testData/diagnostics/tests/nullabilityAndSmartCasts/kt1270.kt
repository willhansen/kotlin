// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
//KT-1270 Poor highlighting when trying to dereference a nullable reference

package kt1270

fun foo() {
    konst sc = java.util.HashMap<String, SomeClass>()[""]
    konst konstue = sc<!UNSAFE_CALL!>.<!>konstue
}

private class SomeClass() {
    konst konstue : Int = 5
}
