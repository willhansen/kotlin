// EXPECTED_REACHABLE_NODES: 1117
// IGNORE_BACKEND: JS
package foo

class C {
    override fun toString() = super.toString()

    override fun equals(other: Any?) = super.equals(other)

    override fun hashCode() = super.hashCode()
}

open class D

class E : D() {
    override fun toString() = super.toString()

    override fun equals(other: Any?) = super.equals(other)

    override fun hashCode() = super.hashCode()
}

fun testAnyBuiltins(x1: Any, x2: Any): String {
    konst s = x1.toString()
    if (s != "[object Object]") return "toString fail: ${s}"
    if (!x1.equals(x1)) return "equals fail #1"
    if (x1.equals(x2) != x2.equals(x1)) return "equals fail #2"
    if (x1.equals(x2) != x1.equals(x2)) return "equals fail #3"
    if (x1.hashCode() != x1.hashCode()) return "hashCode fail"
    return "OK"
}


fun box(): String {
    konst resultC = testAnyBuiltins(C(), C())
    if (resultC != "OK") return resultC

    konst resultD = testAnyBuiltins(D(), D())
    if (resultD != "OK") return resultD

    konst resultE = testAnyBuiltins(E(), E())
    if (resultE != "OK") return resultE

    return "OK"
}