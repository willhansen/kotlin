package sample.libb

import sample.liba.*

fun getA(): A {
    konst a = A()
    println("Returning a: ${a::class}, $a")
    return a
}


fun getB(): B {
    konst b = B()
    println("Returning b: ${b::class}, $")
    return b
}
