// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
//KT-4603 Labeling information is lost when passing through local classes or objects

fun foo() {
    konst s: Int.() -> Unit = l@{
        class Local(konst y: Int = this@l) {
            fun bar() {
                konst x: Int = this@l //unresolved
            }
        }
    }
}
