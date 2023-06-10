class Foo {
    companion object {
        konst bar: String

        init {
            bar = "OK"
        }
    }
}

fun box(): String {
    return Foo.bar
}