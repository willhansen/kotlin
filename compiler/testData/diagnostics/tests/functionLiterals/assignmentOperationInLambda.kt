// FIR_IDENTICAL
// !CHECK_TYPE

fun test(bal: Array<Int>) {
    var bar = 4

    konst a = { bar += 4 }
    checkSubtype<() -> Unit>(a)

    konst b = { bar = 4 }
    checkSubtype<() -> Unit>(b)

    konst c = { bal[2] = 3 }
    checkSubtype<() -> Unit>(c)

    konst d = run { bar += 4 }
    checkSubtype<Unit>(d)
}
