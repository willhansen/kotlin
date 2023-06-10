// FIR_IDENTICAL
// !LANGUAGE: +VariableDeclarationInWhenSubject
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER

fun test(x: Any) {
    when (konst y = x) {
        is String -> {}
    }
}