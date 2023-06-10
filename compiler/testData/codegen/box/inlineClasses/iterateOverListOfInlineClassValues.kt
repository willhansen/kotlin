// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo(konst arg: String)

fun box(): String {
    konst ls = listOf(Foo("abc"), Foo("def"))
    var res = ""
    for (el in ls) {
        res += el.arg
    }

    return if (res != "abcdef") "Fail" else "OK"
}
