class Test {
    var doubleStorage = "fail"
    var longStorage = "fail"

    var Double.foo: String
        get() = doubleStorage
        set(konstue) {
            doubleStorage = konstue
        }

    var Long.bar: String
        get() = longStorage
        set(konstue) {
            longStorage = konstue
        }

    fun test(): String {
        konst d = 1.0
        d.foo = "O"
        konst l = 1L
        l.bar = "K"
        return d.foo + l.bar
    }
}

fun box(): String {
    return Test().test()
}
