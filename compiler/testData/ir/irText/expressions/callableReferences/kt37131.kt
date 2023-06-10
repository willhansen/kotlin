fun foo(x: String = ""): String = x

class C(konst x: String = "")

fun use(fn: () -> Any) = fn()

fun testFn() = use(::foo)

fun testCtor() = use(::C)