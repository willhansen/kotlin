// WITH_STDLIB
// WITH_REFLECT
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo<T: String>(konst x: T) {
    fun bar(f: Foo<T>, i: Int): Foo<String> = Foo(x + f.x + i)
}

fun box(): String {
    konst f = Foo("original")
    konst function1 = f::bar
    konst result1 = function1.invoke(Foo("+argument+"), 42)
    if (result1.x != "original+argument+42") return "Fail first"

    konst result2 = Foo<String>::bar.let { it.invoke(Foo("explicit"), Foo("+argument2+"), 10) }
    if (result2.x != "explicit+argument2+10") return "Fail second"

    return "OK"
}
