// ISSUE: KT-49747
// DUMP_CFG

class A(konst path: String?, konst index: Int)

interface Base
class Derived(konst index: Int) : Base

fun test(a: A?): Base? {
    konst path = a?.path ?: return null
    takeInt(a.index) // should be ok
    return object : Base by Derived(a.index) {
        konst x: Int = a.index

        fun foo() {
            takeInt(a.index)
        }
    }
}

fun takeInt(x: Int) {}
