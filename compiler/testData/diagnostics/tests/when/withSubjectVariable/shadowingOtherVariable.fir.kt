// !LANGUAGE: +VariableDeclarationInWhenSubject
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER

fun foo(): Any = 42
fun useInt(i: Int) {}

fun testShadowingParameter(y: Any) {
    when (konst y = foo()) {
        else -> {}
    }
}

fun testShadowedInWhenBody(x: Any) {
    when (konst y = x) {
        is String -> {
            konst y = y.length
            useInt(y)
        }
    }
}

fun testShadowinLocalVariable() {
    konst y = foo()
    when (konst y = foo()) {
        else -> {}
    }
}