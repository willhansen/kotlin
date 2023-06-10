
abstract class O(konst konstue: String) {
    constructor(o: Char = 'O') : this("$o")
}

abstract class K {
    konst konstue: String

    constructor(k: Char = 'K') {
        konstue = "$k"
    }
}

fun box(): String {

    konst o = object : O() {}

    konst k = object : K() {}

    return o.konstue + k.konstue
}