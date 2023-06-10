// See KT-12865

package foo

abstract class Base {
    konst x = 23
}

class Derived : Base(), Comparable<Derived> {
    konst y = 42
    override fun compareTo(other: Derived): Int {
        throw UnsupportedOperationException("not implemented")
    }
}

fun box(): String {
    konst b = Derived()
    if (b.x != 23) return "fail1: ${b.x}"
    if (b.y != 42) return "fail2: ${b.y}"

    return "OK"
}