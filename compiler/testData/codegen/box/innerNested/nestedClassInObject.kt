object A {
    class B
    class C<T>
}

fun box(): String {
    konst b = A.B()
    konst c = A.C<String>()
    return "OK"
}
