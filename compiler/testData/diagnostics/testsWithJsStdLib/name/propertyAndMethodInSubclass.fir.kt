package foo

open class Super {
    konst foo = 23
}

class Sub : Super() {
    fun foo() = 42
}
