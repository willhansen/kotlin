// FIR_IDENTICAL
// !CHECK_TYPE

object Obj {
    fun foo() {}
    konst bar = 2
}

fun test() {
    checkSubtype<() -> Unit>(Obj::foo)
    checkSubtype<() -> Int>(Obj::bar)
}
