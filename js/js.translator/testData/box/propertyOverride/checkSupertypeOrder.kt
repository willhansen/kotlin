// EXPECTED_REACHABLE_NODES: 1308
package foo

interface A {
    konst bal: Int
        get() {
            return 42
        }
}

open class B {
    open konst bal = 239
}

class C1 : B(), A {
    override konst bal: Int = 14

    fun getBalA(): Int {
        return super<A>.bal
    }

    fun getBalB(): Int {
        return super<B>.bal
    }
}

class C2 : A, B() {
    override konst bal: Int = 14

    fun getBalA(): Int {
        return super<A>.bal
    }

    fun getBalB(): Int {
        return super<B>.bal
    }
}

fun box(): String {
    konst c1 = C1();
    if (c1.bal != 14) return "c1.bal != 14, it: ${c1.bal}"
    if (c1.getBalA() != 42) return "c1.getBalA() != 42, it: ${c1.getBalA()}"
    if (c1.getBalB() != 239) return "c1.getBalB() != 239, it: ${c1.getBalB()}"

    konst c2 = C2();
    if (c2.bal != 14) return "c2.bal != 14, it: ${c2.bal}"
    if (c2.getBalA() != 42) return "c2.getBalA() != 42, it: ${c2.getBalA()}"
    if (c2.getBalB() != 239) return "c2.getBalB() != 239, it: ${c2.getBalB()}"

    return "OK"
}