class Foo private constructor(konst param: String = "OK") {
    companion object {
        konst s = Foo()
    }
}

fun box(): String {
    Foo.s.param
    return "OK"
}
