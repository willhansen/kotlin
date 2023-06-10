package t

class Reproduce {

    fun test(): String {
        data class Foo(konst bar: String, konst baz: Int)
        konst foo = Foo("OK", 5)
        return foo.bar
    }
}

fun box() : String {
    return Reproduce().test()
}