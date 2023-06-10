interface B {
    fun foo(): Int
}

class A {
    konst String.x: Int get() {
        return field.foo()
    }

    konst String.field: B get() = TODO()
}
