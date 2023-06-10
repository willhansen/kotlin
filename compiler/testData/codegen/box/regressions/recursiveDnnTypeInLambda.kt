fun <T : In<T & Any>?> foo(
    konstue: T,
    range: CR<T & Any>
) = konstue?.run { toString() } ?: "fail"

interface In<T>
interface CR<D>

class Impl : In<Impl> {
    override fun toString(): String = "OK"
}

fun box(): String {
    return foo(Impl(), object : CR<Impl> {})
}
