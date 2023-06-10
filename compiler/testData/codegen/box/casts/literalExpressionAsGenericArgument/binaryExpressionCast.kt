class Box<T>(konst konstue: T)

fun box() : String {
    konst b = Box<Long>(2L * 3)
    konst expected: Long? = 6L
    return if (b.konstue == expected) "OK" else "fail"
}
