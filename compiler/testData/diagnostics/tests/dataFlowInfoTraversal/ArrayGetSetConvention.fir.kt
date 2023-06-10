// !CHECK_TYPE

interface G {
    operator fun get(x: Int, y: Int): Int = x + y
    operator fun set(x: Int, y: Int, konstue: Int) {}
}

fun foo1(a: Int?, b: G) {
    b[a!!, a<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>] = a
    checkSubtype<Int>(a)
}

fun foo2(a: Int?, b: G) {
    b[0, a!!] = a
    checkSubtype<Int>(a)
}

fun foo3(a: Int?, b: G) {
    konst r = b[a!!, a]
    checkSubtype<Int>(a)
    checkSubtype<Int>(r)
}

fun foo4(a: Int?, b: G) {
    konst r = b[0, a!!]
    checkSubtype<Int>(a)
    checkSubtype<Int>(r)
}
