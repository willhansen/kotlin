// FIR_IDENTICAL
konst <T> T.foo : T?
    get() = null

fun test(): Int? {
    return 1.foo
}