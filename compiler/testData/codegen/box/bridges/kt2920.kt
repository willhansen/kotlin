interface Tr<T> {
    konst v: T
}

class C : Tr<String> {
    override konst v = "OK"
}

fun box() = C().v
