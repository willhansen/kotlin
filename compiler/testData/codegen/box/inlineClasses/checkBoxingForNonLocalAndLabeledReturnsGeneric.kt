// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ULong<T: Long>(konst l: T)

fun nonLocal(): ULong<Long>? {
    konst u1 = ULong(1)

    run {
        return u1 // box
    }

    ULong(-1)
}

fun foo(): Boolean = true

fun labeled(): ULong<Long>? {
    konst u = ULong(2)
    return run {
        if (foo()) return@run u
        ULong(-1) // box
    }
}

fun box(): String {
    if (nonLocal()!!.l != 1L) return "fail"
    if (labeled()!!.l != 2L) return "fail"
    return "OK"
}
