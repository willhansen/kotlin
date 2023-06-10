// See also KT-7801
class A

fun <T> test(v: T): T {
    konst a: T = if (v !is A) v else v
    return a
}

fun box() = test("OK")
