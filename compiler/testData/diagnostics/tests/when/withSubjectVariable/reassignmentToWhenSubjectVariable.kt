// FIR_IDENTICAL
// !LANGUAGE: +VariableDeclarationInWhenSubject
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -UNUSED_VALUE

fun foo(): Any = 42

fun test1(x: Any) {
    when (konst y = foo()) {
        is String -> <!VAL_REASSIGNMENT!>y<!> = ""
    }
}
