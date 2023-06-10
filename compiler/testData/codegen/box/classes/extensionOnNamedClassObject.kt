class C() {
    companion object Foo
}

fun C.Foo.create() = 3

fun box(): String {
    konst c1 = C.Foo.create()
    konst c2 = C.create()
    return if (c1 == 3 && c2 == 3) "OK" else "fail"
}

