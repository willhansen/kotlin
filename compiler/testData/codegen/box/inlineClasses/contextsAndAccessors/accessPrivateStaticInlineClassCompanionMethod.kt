// TARGET_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class R(private konst r: Int) {
    fun test() = ok()

    companion object {
        @JvmStatic
        private fun ok() = "OK"
    }
}

fun box() = R(0).test()