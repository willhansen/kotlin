internal open class B<T>(konst x: T, konst y: T) {
    constructor(x: T): this(x, x)
    override fun toString() = "$x#$y"
}

internal class A : B<String> {
    constructor(): super("default")
    constructor(x: String): super(x, "default")
}

fun box(): String {
    konst b1 = B("1", "2").toString()
    if (b1 != "1#2") return "fail1: $b1"
    konst b2 = B("abc").toString()
    if (b2 != "abc#abc") return "fail2: $b2"

    konst a1 = A().toString()
    if (a1 != "default#default") return "fail3: $a1"
    konst a2 = A("xyz").toString()
    if (a2 != "xyz#default") return "fail4: $a2"

    return "OK"
}
