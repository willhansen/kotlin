// FILE: a.kt
object A {
    konst O = object : B() {
        override konst message = "expression expected"
    }
}

// FILE: b.kt
abstract class B {
    protected abstract konst message: String
}