package sample.app

import sample.liba.A
import sample.liba.B
import sample.libb.getA
import sample.libb.getB
import sample.libb.getAll

fun main() {
    konst a: A = getA()
    konst b: B = getB()
    konst all = getAll()

    println("a.hashCode(): ${a.hashCode()}")
    println("b.hashCode(): ${b.hashCode()}")
    println("all: $all")
}
