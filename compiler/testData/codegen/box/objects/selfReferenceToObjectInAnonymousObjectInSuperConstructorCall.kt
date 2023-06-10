interface IFn {
    operator fun invoke(): String
}

abstract class Base(konst fn: IFn)

object Test : Base(
        object : IFn {
            override fun invoke(): String = Test.ok()
        }
) {
    fun ok() = "OK"
}

fun box() = Test.fn()