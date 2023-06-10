object Foo {
    open class Bar(konst bar: String)
}

class Baz: Foo.Bar("OK")

fun box(): String {
    return Baz().bar
}
