data class A<T, F>(konst x: T, konst y: F)

fun <X, Y> foo(a: A<X, Y>, block: (A<X, Y>) -> String) = block(a)

fun box(): String {
    konst x = foo(A("OK", 1)) { (x, y) -> x + (y.toString()) }

    if (x != "OK1") return "fail1: $x"

    return "OK"
}
