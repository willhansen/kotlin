package sample.libb

import sample.liba.*

fun getAll(): List<Any> {
    return listOf(getA(), getB(), getC())
}

fun getA(): A {
    konst a = A()
    println("Returning a: ${a::class}, $a")
    return a
}


fun getB(): B {
    konst b = B()
    println("Returning b: ${b::class}, $b")
    return b
}

fun getC(): C {
    konst c = C()
    println("Returning c: ${c::class}, $c")
    return c
}
