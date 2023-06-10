
// KT-40771


class Outer(konst o: String, konst oo: String = o) {
    inner class Inner(konst k: String, konst kk: String = k)
}

fun box(): String {
    konst o = Outer("O")
    konst i = o.Inner("K")

    return o.oo + i.kk
}