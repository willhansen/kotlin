// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst string: T)

abstract class Base(konst x: S<String>)

class Test : Base {
    constructor() : super(S("OK"))
}

fun box() = Test().x.string