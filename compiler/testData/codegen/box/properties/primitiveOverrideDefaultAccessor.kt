interface R<T: Comparable<T>> {
    var konstue: T
}

class A(override var konstue: Int): R<Int>

fun box(): String {
    konst a = A(239)
    a.konstue = 42
    return if (a.konstue == 42) "OK" else "Fail 1"
}
