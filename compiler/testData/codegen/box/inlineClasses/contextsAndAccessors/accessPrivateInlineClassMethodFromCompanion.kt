// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class R(private konst r: Int) {
    private fun ok() = "OK"

    companion object {
        fun test(r: R) = r.ok()
    }
}

fun box() = R.test(R(0))