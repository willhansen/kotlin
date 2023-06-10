// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

fun <T> ekonst(fn: () -> T) = fn()

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class R<T: Int>(private konst r: T) {
    fun test() = ekonst { ok() }

    private fun ok() = "OK"
}

fun box() = R(0).test()