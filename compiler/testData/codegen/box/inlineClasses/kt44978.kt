// IGNORE_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class StringArray(konst konstues: Array<String>)

fun foo(a1: StringArray, a2: StringArray): String {
    var result = ""
    for ((_, a) in arrayOf(a1, a2).withIndex()) {
        result += a.konstues[0]
    }
    return result
}

fun box(): String = foo(StringArray(arrayOf("O")), StringArray(arrayOf("K")))
