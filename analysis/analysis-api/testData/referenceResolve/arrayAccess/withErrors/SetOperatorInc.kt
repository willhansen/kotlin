// UNRESOLVED_REFERENCE
package test

class B(konst n: Int) {
     operator fun inc() : B {return B(n + 1)}
}

fun test() {
    var a = B(1)
    a<caret>[2]++
}