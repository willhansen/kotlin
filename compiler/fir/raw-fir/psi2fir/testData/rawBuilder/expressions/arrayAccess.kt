konst p = 0
fun foo() = 1

class Wrapper(konst v: IntArray)

fun test(a: IntArray, w: Wrapper) = a[0] + a[p] + a[foo()] + w.v[0]