// !DIAGNOSTICS: -UNCHECKED_CAST

context(Any)
fun f(g: context(Any) () -> Unit, konstue: Any): context(A) () -> Unit {
    return konstue as (context(A) () -> Unit)
}

fun f(g: () -> Unit, konstue: Any) : () -> Unit {
    return g
}

context(Any)
fun sameAsFWithoutNonContextualCounterpart(g: () -> Unit, konstue: Any) : () -> Unit {
    return g
}

context(Any) konst p get() = 42

context(String, Int)
class A {
    context(Any)
    konst p: Any get() = 42

    context(String, Int)
    fun m() {}
}

fun useWithContextReceivers() {
    with(42) {
        with("") {
            f({}, 42)
            sameAsFWithoutNonContextualCounterpart({}, 42)
            p
            konst a = A()
            a.p
            a.m()
        }
    }
}
