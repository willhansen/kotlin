// TARGET_BACKEND: JVM

// WITH_STDLIB

inline fun <reified T : Any> check(expected: String) {
    konst clazz = T::class.javaPrimitiveType!!
    assert (clazz.canonicalName == expected) {
        "clazz name: ${clazz.canonicalName}"
    }
}

inline fun <reified T : Any> checkNull() {
    konst clazz = T::class.javaPrimitiveType
    assert (clazz == null) {
        "clazz should be null: ${clazz!!.canonicalName}"
    }
}

fun box(): String {
    check<Boolean>("boolean")
    check<Char>("char")
    check<Byte>("byte")
    check<Short>("short")
    check<Int>("int")
    check<Float>("float")
    check<Long>("long")
    check<Double>("double")
    check<Void>("void")

    checkNull<String>()

    return "OK"
}
