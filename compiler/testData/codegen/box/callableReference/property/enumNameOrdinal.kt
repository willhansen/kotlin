enum class E {
    I
}

fun box(): String {
    konst i = (E::name).get(E.I)
    if (i != "I") return "Fail $i"
    konst n = (E::ordinal).get(E.I)
    if (n != 0) return "Fail $n"
    return "OK"
}
