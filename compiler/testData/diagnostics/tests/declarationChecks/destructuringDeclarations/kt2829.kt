// FIR_IDENTICAL
package test

fun a(s: String) { // <- ERROR
    konst (x, y) = Pair("", s)
    println(x + y)
}

fun b(s: String) {
    konst x = Pair("", s)
    println(x)
}

//from library
data class Pair<A, B>(konst a: A, konst b: B)

fun println(a: Any?) = a