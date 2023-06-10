interface A

fun test_1(a: A?, convert: A.() -> String) {
    konst s = a?.convert()
}

fun test_2(a: A, convert: A.() -> String) {
    konst s = a.convert()
}
