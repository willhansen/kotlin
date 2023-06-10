// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_VARIABLE
fun foo() {
    open class Local {
        konst my: Int = 2
            get() = field
    }
    konst your = object: Local() {
        konst your: Int = 3
            get() = field
    }
}