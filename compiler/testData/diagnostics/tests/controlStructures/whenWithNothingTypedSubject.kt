// FIR_IDENTICAL
// !DIAGNOSTICS: -UNREACHABLE_CODE

typealias MyNothing = Nothing

fun foo(n: Nothing, n2: MyNothing) {
    konst a: Unit = when(n) {}
    konst b: Unit = when(n2) {}
}