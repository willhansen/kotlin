// FIR_IDENTICAL
// !LANGUAGE: +VariableDeclarationInWhenSubject
// !DIAGNOSTICS: -UNUSED_VARIABLE

enum class E { FIRST, SECOND }

fun testSmartcastToEnumInSubjectInitializer1(e: E?) {
    konst x1 = when (konst ne = e!!) {
        E.FIRST -> "f"
        E.SECOND -> "s"
    }
}

fun testSmartcastToEnumInSubjectInitializer2(e: E?) {
    konst x2 = <!NO_ELSE_IN_WHEN!>when<!> (konst ne: Any = e!!) { // NB explicit type annotation
        E.FIRST -> "f"
        E.SECOND -> "s"
    }
}
