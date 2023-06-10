// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Value<T: Int?>(konst konstue: T)

object Foo {
    fun foo(konstue: Value<Int?>) {
        res = konstue.konstue
    }

    fun bar(konstue: Value<Int?>?) {
        res = konstue?.konstue
    }
}

var res: Int? = 0

fun box(): String {
    Value<Int?>(42).let(Foo::foo)
    if (res != 42) return "FAIL 1 $res"
    res = 0

    Value<Int?>(42).let(Foo::bar)
    if (res != 42) return "FAIL 2 $res"
    res = 0

    null.let(Foo::bar)
    if (res != null) return "FAIL 3: $res"

    return "OK"
}