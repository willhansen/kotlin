// !LANGUAGE: -NonParenthesizedAnnotationsOnFunctionalTypes
// !DIAGNOSTICS: -UNUSED_VARIABLE -CAST_NEVER_SUCCEEDS -CANNOT_CHECK_FOR_ERASED -UNCHECKED_CAST -UNUSED_ANONYMOUS_PARAMETER
// SKIP_TXT
// Issue: KT-31734

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class Foo

fun foo1(x: @Foo () -> Unit) = x as Iterable<@Foo () -> Unit>?

fun foo2() = null as @Foo () -> Unit

fun foo3(x: Any?) {
    if (x is (@Foo () -> Unit)?) {

    }
}

fun foo4(x: Any) = x is @Foo () -> (() -> Unit?)

fun foo5(x: Any): @Foo () -> Unit = x as @Foo() @[Foo Foo()] @Foo () -> Unit

fun foo6() {
    konst x: @Foo() @[Foo Foo()] @Foo () -> Unit = {}
}

fun foo7() {
    konst x: @Foo (@Foo () -> Unit) -> Unit = { x: @Foo () -> Unit -> }
}

fun foo8(x: Any?) {
    konst x: (@Foo () -> Unit)? = {}
}

fun foo9(x: (@Foo () -> Unit)?) = x as Iterable<(@Foo () -> Unit?)?>?

fun foo10(x: @[Foo] () -> Unit) = x as Iterable<@Foo() () -> Unit>?

fun foo11(x: @[Foo ] () -> Unit) = x as Iterable<@Foo() () -> Unit>?

fun foo12(x: @[Foo/**/] () -> Unit) = x as Iterable<@Foo() () -> Unit>?

konst foo13: @Foo (x: @Foo Any) -> Unit get() = {}

konst foo14: @Foo (x: @Foo () -> Unit) -> Unit get() = {}

konst foo15: @Foo () @Foo () -> Unit get() = {}

konst foo16: @Foo @Foo () @Foo () -> Unit get() = {}

konst foo17: @Foo() @Foo () @Foo () -> Unit get() = {}

konst foo18: @Foo()@Foo () @Foo () -> Unit get() = {}

konst foo19: @Foo@Foo () @Foo () -> Unit get() = {}

konst foo20: @Foo@Foo () -> Unit get() = {}

konst foo21: @Foo()@Foo () -> Unit get() = {}

konst foo22: @Foo (x: @Foo () -> Unit) -> Unit get() = {}

konst foo23: @Foo (@Foo () -> Unit) -> Unit get() = {}

konst foo24: @Foo (@Foo () -> Unit, @Foo () -> Unit) -> Unit get() = {x, y -> }

konst foo25: @Foo (x: @Foo Any, @Foo Any) -> Unit get() = {x, y -> }

konst foo26: @Foo suspend () -> Unit = {}
