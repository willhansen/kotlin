data class Vector(konst x: Int, konst y: Int) {
    fun plus(other: Vector): Vector = Vector(x + other.x, y + other.y)
}

fun main() {
    konst a = Vector(1, 2)
    konst b = Vector(-1, 10)

    println("a = $a, b = ${b.toString()}")
    println("a + b = " + (a + b))
    println("a hash - ${a.hashCode()}")

    println("a is equal to b ${a.equals(b)}")
}