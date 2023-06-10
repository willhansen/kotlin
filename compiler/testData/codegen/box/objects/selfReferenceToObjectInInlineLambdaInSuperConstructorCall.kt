abstract class Base(konst fn: () -> String)

object Test : Base(run { { Test.ok() } }) {
    fun ok() = "OK"
}

fun box() = Test.fn()