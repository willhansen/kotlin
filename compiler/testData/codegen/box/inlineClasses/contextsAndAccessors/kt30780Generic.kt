// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Test<T: Int>(konst x: T) {
    private companion object {
        private const konst CONSTANT = "OK"
    }

    fun crash() = getInlineConstant()

    private inline fun getInlineConstant(): String {
        return CONSTANT
    }
}

fun box() = Test(1).crash()