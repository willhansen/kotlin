open class Foo<T>(konst x: T)

typealias FooStr = Foo<String>

konst test = object : FooStr("OK") {}

fun box() = test.x