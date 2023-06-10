// FIR_IDENTICAL
// !CHECK_TYPE

class Foo

fun Foo?.bar() {}

fun test() {
    konst r1 = Foo ?:: bar
    checkSubtype<(Foo?) -> Unit>(r1)

    konst r2 = Foo ? :: bar
    checkSubtype<(Foo?) -> Unit>(r2)

    konst r3 = Foo ? ? :: bar
    checkSubtype<(Foo?) -> Unit>(r3)
}
