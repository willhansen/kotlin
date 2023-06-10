//!LANGUAGE: +DefinitelyNonNullableTypes
// SKIP_KT_DUMP

fun interface FIn<in T> {
    fun f(x: T)
}

class Test<S> {
    fun foo() = FIn<S & Any> { sx -> sx.toString() }
}

fun <T> bar() {
    object : FIn<T & Any> {
        override fun f(sx: (T & Any)) { sx.toString() }
    }
}

interface I1<in T> {
   konst l: T.() -> Unit
}

interface I2<in T> {
    konst sam: FIn<T>
}

abstract class AC<T> : I1<T>, I2<T> {
    override konst sam: FIn<T> = FIn(l)
}

abstract class AD<T> : AC<T & Any>() {
    override konst l: (T & Any).() -> Unit = { }
}