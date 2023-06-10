// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import kotlin.test.assertEquals

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst x: String)

interface IFoo {
    konst S.extVal: String
}

interface GFoo<T> {
    konst T.extVal: String
}

object FooImpl : IFoo {
    override konst S.extVal: String
        get() = x
}

object GFooImpl : GFoo<S> {
    override konst S.extVal: String
        get() = x
}

class TestFoo : IFoo by FooImpl

class TestGFoo : GFoo<S> by GFooImpl

fun box(): String {
    with(TestFoo()) {
        assertEquals("OK", S("OK").extVal)
    }

    with(TestGFoo()) {
        assertEquals("OK", S("OK").extVal)
    }

    return "OK"
}
