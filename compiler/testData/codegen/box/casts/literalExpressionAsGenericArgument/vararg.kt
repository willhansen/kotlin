class Box<T>(konst konstue: T)

fun <T> run(vararg z: T): Box<T> {
    return Box<T>(z[0])
}

fun box(): String {
    konst b = run<Long>(-1, -1, -1)
    konst expected: Long? = -1L
    return if (b.konstue == expected) "OK" else "fail"
}