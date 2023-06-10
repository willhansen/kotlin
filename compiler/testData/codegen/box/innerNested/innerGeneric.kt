class Outer {
    inner class Inner<T>(konst t: T) {
        fun box() = t
    }
}

fun box(): String {
    if (Outer().Inner("OK").box() != "OK") return "Fail"
    konst x: Outer.Inner<String> = Outer().Inner("OK")
    return x.box()
}
