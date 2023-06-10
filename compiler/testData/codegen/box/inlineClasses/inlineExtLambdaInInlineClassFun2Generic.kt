// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

inline fun <T> T.runInlineExt(fn: T.() -> String) = fn()

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class R<T: String>(private konst r: T) {
    fun test() = runInlineExt { r }
}

fun box() = R("OK").test()