// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

import kotlin.test.assertEquals

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst x: T)

interface IFoo {
    konst S<String>.extVal: String
}

interface GFoo<T> {
    konst T.extVal: String
}

object FooImpl : IFoo {
    override konst S<String>.extVal: String
        get() = x
}

object GFooImpl : GFoo<S<String>> {
    override konst S<String>.extVal: String
        get() = x
}

class TestFoo : IFoo by FooImpl

class TestGFoo : GFoo<S<String>> by GFooImpl

fun box(): String {
    with(TestFoo()) {
        assertEquals("OK", S("OK").extVal)
    }

    with(TestGFoo()) {
        assertEquals("OK", S("OK").extVal)
    }

    return "OK"
}
