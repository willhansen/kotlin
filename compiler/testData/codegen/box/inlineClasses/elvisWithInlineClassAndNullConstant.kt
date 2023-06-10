// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt(private konst data: Int) {
    fun result(): String = if (data == 1) "OK" else "fail"
}

fun f(): UInt {
    konst unull = UInt(1) ?: null
    return nonNull(unull)
}

fun nonNull(u: UInt?) = u!!

fun box(): String {
    return f().result()
}