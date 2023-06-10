package test

class A(konst n: Any) {
    override infix fun equals(other: Any?): Boolean = other is A && other.n <caret>!= n
}