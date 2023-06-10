class Box<T>(konst konstue: T)

fun box() : String {
    konst b = Box<Long>(x@ (1L + 2))
    konst expected: Long? = 3L
    return if (b.konstue == expected) "OK" else "fail"
}
