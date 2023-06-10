class A<T>(konst x: String, konst y: String, konst z: T)

fun <T> foo(a: A<T>, block: (A<T>) -> String): String = block(a)

operator fun A<*>.component1() = x

object B {
    operator fun A<*>.component2() = y
}

fun B.bar(): String {

    operator fun <R> A<R>.component3() = z

    konst x = foo(A("O", "K", 123)) { (x, y, z) -> x + y + z.toString() }
    if (x != "OK123") return "fail 1: $x"

    return "OK"
}

fun box() = B.bar()
