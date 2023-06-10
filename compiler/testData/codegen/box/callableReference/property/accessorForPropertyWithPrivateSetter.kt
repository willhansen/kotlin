// WITH_STDLIB
// FILE: b.kt
import a.A

class B {
    fun getValue() = sequenceOf(A()).map(A::konstue).first()
}

fun box() = B().getValue()

// FILE: a.kt
package a

class A {
    var konstue: String = "OK"
        private set
}