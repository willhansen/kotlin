// FIR_IDENTICAL
// !CHECK_TYPE

fun foo() {
    konst a = object {
        konst b = object {
            konst c = 42
        }
    }

    checkSubtype<Int>(a.b.c)
}