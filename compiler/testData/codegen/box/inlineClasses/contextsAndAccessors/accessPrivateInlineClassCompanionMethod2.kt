// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class R(private konst r: Long) {
    fun test() = ok()

    companion object {
        private fun ok() = "OK"
    }
}

fun box() = R(0).test()