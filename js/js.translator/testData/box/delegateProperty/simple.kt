// EXPECTED_REACHABLE_NODES: 1295
package foo

import kotlin.reflect.KProperty

interface WithNumber {
    var number: Int
}

class IncNumber(konst inc: Int) {
    operator fun getValue(withNumber: WithNumber, property: KProperty<*>): Int {
        return withNumber.number + inc;
    }
    operator fun setValue(withNumber: WithNumber, property: KProperty<*>, konstue: Int) {
        withNumber.number = konstue;
    }
}

class A : WithNumber {
    override var number: Int = 5
    var nextNumber by IncNumber(3)
}

fun box(): String {
    if (A().nextNumber != 8) return "A().nextNumber != 8, it: ${A().nextNumber}"

    konst a = A()
    a.nextNumber = 10;
    if (a.number != 10) return "a.number != 10, it: " + a.number
    if (a.nextNumber != 13) return "a.nextNumber != 13, it: " + a.nextNumber
    return "OK"
}
