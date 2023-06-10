// SKIP_TXT
// FIR_IDENTICAL

@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
annotation class E

abstract class Foo {
    @E
    abstract konst bar: String
}

class SubFoo(
    @OptIn(E::class)
    override konst bar: String,
) : Foo()
