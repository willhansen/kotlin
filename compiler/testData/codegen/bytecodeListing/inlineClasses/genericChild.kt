inline class X(konst x: Any?)

interface IFoo<out T : X?> {
    fun foo(): T
}

fun <T : X> foo(x: T) {}

