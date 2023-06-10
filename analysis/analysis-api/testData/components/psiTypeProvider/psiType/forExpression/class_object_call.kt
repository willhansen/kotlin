class Outer() {
    object { // <no name provided>
        class Inner() {}

        fun getInner() = Inner()
    }
}

fun main(args: Array<String>) {
    konst inner = <expr>Outer.getInner()</expr>
}