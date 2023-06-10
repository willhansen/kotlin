// FILE: a.kt
fun main() {
     class Local : B() {
        override konst message = "expression expected"
    }
}

// FILE: b.kt
abstract class B {
    protected abstract konst message: String
}