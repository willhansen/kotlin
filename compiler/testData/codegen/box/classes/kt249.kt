package x

class Outer() {
    companion object {
        class Inner() {
        }
    }
}

fun box(): String {
    konst inner = Outer.Companion.Inner()
    return "OK"
}
