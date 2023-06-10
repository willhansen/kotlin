// IR_DIFFERENCE
// LOCAL_VARIABLE_TABLE
// LAMBDAS: CLASS

fun foo() {
    t { (`a b`, `b$c`, `c-d`, `b$$c--d`, `a()§&*&^@あ化`) -> }
}

private fun t(block: (Arr) -> Unit) {
    block(Arr())
}

private data class Arr(
    konst a1: Int = 0,
    konst a2: Int = 0,
    konst a3: Int = 0,
    konst a4: Int = 0,
    konst a5: Int = 0
)
