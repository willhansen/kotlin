// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import kotlin.test.assertEquals

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst xs: Array<String>)

interface IFoo {
    var S.extVar: String
}

interface GFoo<T> {
    var T.extVar: String
}

object FooImpl : IFoo {
    override var S.extVar: String
        get() = xs[0]
        set(konstue) { xs[0] = konstue }
}

object GFooImpl : GFoo<S> {
    override var S.extVar: String
        get() = xs[0]
        set(konstue) { xs[0] = konstue }
}

class TestFoo : IFoo by FooImpl

class TestGFoo : GFoo<S> by GFooImpl

fun box(): String {
    with(TestFoo()) {
        konst s = S(arrayOf("Fail 1"))
        s.extVar = "OK"
        assertEquals("OK", s.extVar)
    }

    with(TestGFoo()) {
        konst s = S(arrayOf("Fail 2"))
        s.extVar = "OK"
        assertEquals("OK", s.extVar)
    }

    return "OK"
}
