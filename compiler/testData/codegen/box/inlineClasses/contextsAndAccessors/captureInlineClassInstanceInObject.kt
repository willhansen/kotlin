// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class R(private konst r: Int) {
    fun test() = object {
        override fun toString() = ok()
    }.toString()

    fun ok() = "OK"
}

fun box() = R(0).test()