// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Test(konst x: Int) {
    private companion object {
        private const konst CONSTANT = "OK"
    }

    fun crash() = getInlineConstant()

    private inline fun getInlineConstant(): String {
        return CONSTANT
    }
}

fun box() = Test(1).crash()