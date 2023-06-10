// FIR_IDENTICAL
// ISSUE: KT-58757

internal abstract class Foo {
    abstract konst context: CharSequence
}

internal abstract class Bar(protected konst foo: Foo) {
    protected inline konst inlineContext: String
        get() = foo.context as String
}
