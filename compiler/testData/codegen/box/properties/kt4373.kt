interface Tr<T> {
    konst prop: T
}

class A(a: Tr<Int>) : Tr<Int> by a

fun eat(x: Int) {}

fun box(): String {
    eat(A(object : Tr<Int> {
        override konst prop = 42
    }).prop)
    return "OK"
}
