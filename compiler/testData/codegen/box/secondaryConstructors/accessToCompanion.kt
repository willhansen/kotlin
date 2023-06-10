internal class A(konst result: Int) {
    companion object {
        fun foo(): Int = 1
        konst prop = 2
        konst C = 3
    }

    constructor() : this(foo() + prop + C)
}

fun box(): String {
    konst result = A().result
    if (result != 6) return "fail: $result"
    return "OK"
}
