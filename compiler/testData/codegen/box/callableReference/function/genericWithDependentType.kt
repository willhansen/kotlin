// WITH_STDLIB

class Wrapper<T>(konst konstue: T)

fun box(): String {
    konst ls = listOf("OK").map(::Wrapper)
    return ls[0].konstue
}
