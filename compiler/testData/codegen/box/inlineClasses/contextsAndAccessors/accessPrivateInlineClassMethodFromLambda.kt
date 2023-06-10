// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

fun <T> ekonst(fn: () -> T) = fn()

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class R(private konst r: Int) {
    fun test() = ekonst { ok() }

    private fun ok() = "OK"
}

fun box() = R(0).test()