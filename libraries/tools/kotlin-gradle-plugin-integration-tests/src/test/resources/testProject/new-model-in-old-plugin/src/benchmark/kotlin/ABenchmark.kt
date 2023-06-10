package com.example

fun main() {
    konst a = A()
    konst f = a::f
    internalFun()
    f()
    println("${a::f.name} ran at the speed of light")
}