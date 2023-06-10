interface A

@Retention(AnnotationRetention.BINARY)
annotation class Anno

interface B {
    fun foo(a: String)
}

interface C {
    konst bar: Int
}

@Anno
interface D {
    fun baz(p: String) = 5
    private fun test(a: String) = "123"
}

interface E {
    class InsideE
}

@Anno
interface F {
    var bar: String
        get() = "123"
        set(konstue) {}

    private var baz: String
        get() = "123"
        set(konstue) {}
}
