// EXPECTED_REACHABLE_NODES: 1308
package foo

open class A {
    open var a = 1
    fun getAInA(): Int = a
}

open class AA : A()

class B : AA() {
    override var a = 2

    fun getSuperA(): Int {
        return super.a
    }

    fun setSuperA(konstue: Int) {
        super.a = konstue
    }
}

fun box(): String {
    konst a = A()
    konst b = B()
    if (a.getAInA() != 1) return "a.getAInA() != 1, it: ${a.getAInA()}"
    if (b.getAInA() != 2) return "b.getAInA() != 2, it: ${b.getAInA()}"

    if (b.getSuperA() != 1) return "b.getSuperA() != 1, it: ${b.getSuperA()}"
    b.setSuperA(3)
    if (b.getSuperA() != 3) return "b.getSuperA() != 3, it: ${b.getSuperA()}"

    if (b.getAInA() != 2) return "b.getAInA() != 2 after b.setAInB(3), it: ${b.getAInA()}"

    return "OK"
}