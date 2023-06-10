annotation class Simple(konst konstue: String)

fun localCaptured(): Any {
    konst z  = 1
    class A(@Simple("K") konst z: String) {
        konst x = z
    }
    return A("K")
}
