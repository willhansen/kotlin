class B {
    fun append() {}
}

class A {
    konst message = B()

    fun foo(w: Boolean) {
        if (w) {
            konst message = ""
            message.toString()
        } else {
            message.append() // message here should relate to the class-level property
        }
    }
}
