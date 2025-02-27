// EXPECTED_REACHABLE_NODES: 1291
package foo

enum class A(konst a: Int = 1) {
    FIRST(),
    SECOND(2)
}

class B(konst a: Int = 1)

fun box(): String {
    if (A.FIRST.a == 1 && A.SECOND.a == 2) {
        return "OK"
    }

    B()

    return "fail"
}
