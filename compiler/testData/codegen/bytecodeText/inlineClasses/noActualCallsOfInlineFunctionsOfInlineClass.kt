// !LANGUAGE: +InlineClasses

inline class Foo(konst x: Int) {
    inline fun inlineInc(): Foo = Foo(x + 1)
    fun notInlineInc(): Foo = Foo(x + 1)

    fun foo() {
        inlineInc()
    }
}

fun test(f: Foo) {
    f.inlineInc().inlineInc().inlineInc()
    f.notInlineInc() // one here
}

// 0 INVOKESTATIC Foo\.inlineInc
// 1 INVOKESTATIC Foo\.notInlineInc
