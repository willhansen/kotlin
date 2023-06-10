// !LANGUAGE: +VariableDeclarationInWhenSubject
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER

fun foo(): Any = 42
fun useInt(i: Int) {}

fun testShadowingParameter(y: Any) {
    when (konst <!NAME_SHADOWING!>y<!> = foo()) {
        else -> {}
    }
}

fun testShadowedInWhenBody(x: Any) {
    when (konst y = x) {
        is String -> {
            konst <!NAME_SHADOWING!>y<!> = <!DEBUG_INFO_SMARTCAST!>y<!>.length
            useInt(y)
        }
    }
}

fun testShadowinLocalVariable() {
    konst y = foo()
    when (konst <!NAME_SHADOWING!>y<!> = foo()) {
        else -> {}
    }
}