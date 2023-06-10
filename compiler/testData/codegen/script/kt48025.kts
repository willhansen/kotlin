
konst p = 0

class ReducedFraction() {
    fun plus1() = reducedFractionOf(p)
    konst y = 1
}

fun reducedFractionOf(a: Int) {
}

konst c = ReducedFraction()
konst x = c.y
// expected: x: 1
