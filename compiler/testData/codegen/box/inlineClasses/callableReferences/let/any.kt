// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Value(konst konstue: Any)

object Foo {
    fun foo(konstue: Value) {
        res = konstue.konstue as String
    }

    fun bar(konstue: Value?) {
        res = konstue?.konstue as String?
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
    if (res != null) return "FAIL 5: $res"

    return "OK"
}