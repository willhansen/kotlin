// TARGET_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class R<T: Int>(private konst r: T) {
    fun test() = ok()

    companion object {
        @JvmStatic
        private fun ok() = "OK"
    }
}

fun box() = R(0).test()