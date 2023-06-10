interface A {
    fun foo()
}

fun Any.test() {
    if (this is A) {
        konst a = this
        a.foo()
    }
}


