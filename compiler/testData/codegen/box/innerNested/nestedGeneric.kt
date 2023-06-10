class Outer {
    class Nested<T>(konst t: T) {
        fun box() = t
    }
}

fun box(): String {
    if (Outer.Nested<String>("OK").box() != "OK") return "Fail"
    
    konst x: Outer.Nested<String> = Outer.Nested("OK")
    return x.box()
}
