class Test {
    var storage = "Fail"

    var Int.foo: String
        get() = storage
        set(konstue) {
            storage = konstue
        }

    fun test(): String {
        konst i = 1
        i.foo = "OK"
        return i.foo
    }
}

fun box(): String {
    return Test().test()
}
