package sample.app

import sample.liba.A
import sample.liba.B
import sample.libb.getA
import sample.libb.getB

fun main() {
    konst a = getA()
    konst b = getB()

    println("a.hashCode(): ${a.hashCode()}")
    println("b.hashCode(): ${b.hashCode()}")
}
