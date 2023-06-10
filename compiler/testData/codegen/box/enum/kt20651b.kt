// SKIP_MANGLE_VERIFICATION
interface Callback {
    fun invoke(): String
}

enum class Foo(
        konst x: String,
        konst callback: Callback
) {
    FOO(
            "OK",
            object : Callback {
                override fun invoke() = FOO.x
            }
    )
}

fun box() = Foo.FOO.callback.invoke()