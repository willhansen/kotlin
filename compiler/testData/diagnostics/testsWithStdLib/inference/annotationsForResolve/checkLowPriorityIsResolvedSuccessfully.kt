// FIR_IDENTICAL
class Foo {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @kotlin.internal.LowPriorityInOverloadResolution
    konst test: Bar = Bar()
}

fun Foo.test() {}
class Bar
class Scope {
    operator fun Bar.invoke(f: () -> Unit) {}
}

fun Scope.bar(e: Foo) {
    e.test {}
}

class Baz
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
constructor(konst x: Foo)

fun Baz(x: Foo): Baz {
    throw NotImplementedError()
}

fun testBaz(e: Foo) = Baz(e)
