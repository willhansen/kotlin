class Test {
    konst Int.foo: String
        get() = "OK"

    fun test(): String {
        return 1.foo
    }
}

fun box(): String {
    return Test().test()
}
