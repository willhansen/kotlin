data class A(konst o: String, konst k: String) {
    constructor() : this("O", "k")
}

fun box(): String {
    konst a = A().copy(k = "K")
    return a.o + a.k
}

