package t

class A {
    companion object Named {
        konst i: Int = 10
    }
}

fun f() {
    <caret>A.Named.i
}