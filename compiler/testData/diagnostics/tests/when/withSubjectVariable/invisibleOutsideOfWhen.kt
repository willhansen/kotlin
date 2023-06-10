// FIR_IDENTICAL
// !LANGUAGE: +VariableDeclarationInWhenSubject
// !DIAGNOSTICS: -UNUSED_VARIABLE

fun foo(): Any = 42

fun test(x: Any) {
    konst z1 = when (konst y = foo()) {
        42 -> "Magic: $y, $x"
        else -> {
            "Not magic: $y, $x"
        }
    }
    konst z2 = "Anyway, it was $<!UNRESOLVED_REFERENCE!>y<!>"
}