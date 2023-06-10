// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

fun <T> T.runExt(fn: T.() -> String) = fn()

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class R(private konst r: Int) {
    fun test() = runExt { "OK" }
}

fun box() = R(0).test()