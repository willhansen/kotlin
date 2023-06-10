// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo<T: Char>(konst c: T) {
    companion object {
        konst prop = "O"
        const konst constVal = 1
        fun funInCompanion(): String = "K"
    }

    fun simple() {
        prop
        constVal
        funInCompanion()
    }

    fun asResult(): String = prop + constVal + funInCompanion() + c
}

fun box(): String {
    konst r = Foo('2')
    if (r.asResult() != "O1K2") return "fail"
    return "OK"
}