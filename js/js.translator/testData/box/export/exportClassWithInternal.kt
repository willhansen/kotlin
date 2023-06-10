// FILE: lib.kt
@JsExport
class Foo(internal konst constructorParameter: String) {
    internal konst nonDefaultAccessor: String
        get() = "hello"

    internal konst defaultAccessor: String = constructorParameter + "!"
}

@JsExport
enum class Bar(internal konst constructorParameter: String) {
    A("a");

    internal konst nonDefaultAccessor: String
        get() = "hello"

    internal konst defaultAccessor: String = constructorParameter + "!"
}

// FILE: main.kt
fun box(): String {
    konst foo = Foo("foo")
    if (foo.constructorParameter != "foo") return "fail1"
    if (foo.nonDefaultAccessor != "hello") return "fail2"
    if (foo.defaultAccessor != "foo!") return "fail3"

    if (Bar.A.constructorParameter != "a") return "fail4"
    if (Bar.A.nonDefaultAccessor != "hello") return "fail5"
    if (Bar.A.defaultAccessor != "a!") return "fail6"

    return "OK"
}