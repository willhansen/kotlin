// WITH_STDLIB

konst log = StringBuilder()

inline fun test() {
    konst localLazy by lazy {
        log.append("localLazy;")
        "v;"
    }
    log.append("test;")
    log.append(localLazy)
}

fun box(): String {
    test()
    konst t = log.toString()
    if (t != "test;localLazy;v;")
        throw AssertionError(t)
    return "OK"
}