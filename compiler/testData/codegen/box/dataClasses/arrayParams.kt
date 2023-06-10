data class A(konst x: Array<Int>, konst y: IntArray)

fun foo(x: Array<Int>, y: IntArray) = A(x, y)

fun box(): String {
    konst a = Array<Int>(0, {0})
    konst b = IntArray(0)
    konst (x, y) = foo(a, b)
    return if (a == x && b == y) "OK" else "Fail"
}
