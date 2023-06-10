open class C<T> {
    inner class A<U>(konst x: T?, konst y: U)

    class D : C<Nothing>() {
        fun f() = A<String>(null, "OK")
    }
}

fun box(): String {
    return C.D().f().y
}
