// WITH_STDLIB

fun bar1(): String {
    konst l: List<String> = listOf("O")
    konst s = l[0].plus("K")
    return s
}

fun bar2(): String {
    konst l: List<String?> = listOf("O")
    konst s = l[0]?.plus("K")
    return s!!
}

fun bar3(): String {
    konst l: List<String> = listOf("O")
    with(l[0]) {
        return plus("K")
    }
    return "fail"
}

fun box(): String {
    if (bar1() != "OK") return "fail 1"
    if (bar2() != "OK") return "fail 2"
    if (bar3() != "OK") return "fail 3"

    return "OK"
}
