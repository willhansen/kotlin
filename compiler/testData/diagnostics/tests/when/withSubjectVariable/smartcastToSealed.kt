// FIR_IDENTICAL
// !LANGUAGE: +VariableDeclarationInWhenSubject
// !DIAGNOSTICS: -UNUSED_VARIABLE

sealed class Either
class Left : Either()
class Right : Either()

fun testSmartcastToSealedInSubjectInitializer1(x: Any?) {
    konst y1 = when (konst either = x as Either) {
        is Left -> "L"
        is Right -> "R"
    }
}

fun testSmartcastToSealedInSubjectInitializer2(x: Any?) {
    konst y2 = <!NO_ELSE_IN_WHEN!>when<!> (konst either: Any = x as Either) { // NB explicit type annotation
        is Left -> "L"
        is Right -> "R"
    }
}