// FIR_IDENTICAL
// !LANGUAGE: +VariableDeclarationInWhenSubject
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER


fun testNoSubjectVariableName(x: Int?) {
    konst y = when (konst<!SYNTAX!><!> = 42) {
        0 -> "0"
        else -> "not 0"
    }
}