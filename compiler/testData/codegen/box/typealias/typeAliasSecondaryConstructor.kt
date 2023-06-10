class C(konst x: String) {
    constructor(n: Int) : this(n.toString())
}

typealias Alias = C

fun box(): String {
    konst c = Alias(23)
    if (c.x != "23") return "fail: $c"
    return "OK"
}
