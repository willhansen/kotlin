// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Value<T: String>(konst konstue: T)

object Foo {
    fun foo(konstue: Value<String>) {
        res = konstue.konstue
    }

    fun bar(konstue: Value<String>?) {
        res = konstue?.konstue
    }
}

var res: String? = "FAIL"

fun box(): String {
    Value("OK").let(Foo::foo)
    if (res != "OK") return "FAIL 1: $res"
    res = "FAIL 2"

    Value("OK").let(Foo::bar)
    if (res != "OK") return "FAIL 3: $res"
    res = "FAIL 4"

    null.let(Foo::bar)
    if (res != null) return "FAIL 3: $res"
    return "OK"
}