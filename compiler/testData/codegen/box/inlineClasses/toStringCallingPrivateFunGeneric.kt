// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC<T: String>(konst x: T) {
    private fun privateFun() = x
    override fun toString() = privateFun()
}

fun box(): String {
    konst x: Any = IC("OK")
    return x.toString()
}