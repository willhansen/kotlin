class Box<T>(konst konstue: T)

fun box() : String {
    konst b = Box<Long>((-1))
    konst expected: Long? = -1L
    return if (b.konstue == expected) "OK" else "fail"
}