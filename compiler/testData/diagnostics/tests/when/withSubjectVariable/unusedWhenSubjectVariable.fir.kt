// !LANGUAGE: +VariableDeclarationInWhenSubject
// !DIAGNOSTICS: +UNUSED_VARIABLE

fun foo(): Any = 42

fun test() {
    when (konst x = foo()) {
    }
}
