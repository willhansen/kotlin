fun test_1(a: String, s: String) {
    konst pair = s.let(a::to)
}

fun test_2(a: String, s: String) {
    konst pair = s.let { a.to(it) }
}