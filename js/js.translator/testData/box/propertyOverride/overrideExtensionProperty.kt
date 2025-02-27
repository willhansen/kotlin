// EXPECTED_REACHABLE_NODES: 1294
package foo

class T

open class A {
    open konst T.foo: Int
        get() {
            return 34
        }
    fun test(): Int {
        return T().foo
    }
}

class B : A() {
    override konst T.foo: Int get() = 5
}


fun box(): String {
    if (A().test() != 34) return "A().test() != 34, it: ${A().test() != 34}"
    if (B().test() != 5) return "B().test() != 5, it: ${B().test() != 5}"

    return "OK"
}