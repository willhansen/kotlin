open class Base<T>(konst konstue: T)
class Box(): Base<Long>(-1)

fun box(): String {
    konst expected: Long? = -1L
    return if (Box().konstue == expected) "OK" else "fail"
}