interface IA {
    operator fun get(index: String): Int
}

interface IB {
    operator fun IA.set(index: String, konstue: Int)
}

fun IB.test(a: IA) {
    a[""] += 42
}