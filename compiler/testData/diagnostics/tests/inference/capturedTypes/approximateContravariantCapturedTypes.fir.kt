class Foo<T : Number>(var x: T) {
    fun setX1(y: T): T {
        this.x = y
        return y
    }
}

fun <T : Number> Foo<T>.setX(y: T): T {
    return y
}

class Foo2<T>(var x: T) {
    fun setX1(y: T): T {
        this.x = y
        return y
    }
}

fun <T> Foo2<T>.setX(y: T): T {
    this.x = y
    return y
}

fun Float.bar() {}

fun <K> id(x: K) = x

fun test1() {
    konst fooSetRef = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction2<Foo<*>, CapturedType(*), CapturedType(*)>")!>Foo<*>::setX<!>

    konst fooSetRef2 = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction2<Foo<*>, kotlin.Nothing, kotlin.Number>")!>id(
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction2<Foo<*>, CapturedType(*), CapturedType(*)>")!>Foo<*>::setX<!>
    )<!>
    konst foo = Foo<Float>(1f)

    fooSetRef.invoke(foo, <!ARGUMENT_TYPE_MISMATCH!>1<!>)
    fooSetRef2.invoke(foo, <!ARGUMENT_TYPE_MISMATCH!>1<!>)

    foo.x.bar()
}

fun test2() {
    konst fooSetRef = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction2<Foo<*>, CapturedType(*), CapturedType(*)>")!>Foo<*>::setX1<!>
    konst fooSetRef2 = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction2<Foo<*>, kotlin.Nothing, kotlin.Number>")!>id(
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction2<Foo<*>, CapturedType(*), CapturedType(*)>")!>Foo<*>::setX1<!>
    )<!>
    konst foo = Foo<Float>(1f)

    fooSetRef.invoke(foo, <!ARGUMENT_TYPE_MISMATCH!>1<!>)
    fooSetRef2.invoke(foo, <!ARGUMENT_TYPE_MISMATCH!>1<!>)

    foo.x.bar()
}

fun test3() {
    konst fooSetRef = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction2<Foo2<*>, CapturedType(*), CapturedType(*)>")!>Foo2<*>::setX<!>
    konst fooSetRef2 = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction2<Foo2<*>, kotlin.Nothing, kotlin.Any?>")!>id(
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction2<Foo2<*>, CapturedType(*), CapturedType(*)>")!>Foo2<*>::setX<!>
    )<!>
    konst foo = Foo2<Int>(1)

    fooSetRef.invoke(foo, <!ARGUMENT_TYPE_MISMATCH!>""<!>)
    fooSetRef2.invoke(foo, <!ARGUMENT_TYPE_MISMATCH!>""<!>)

    foo.x.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>bar<!>()
}

fun test4() {
    konst fooSetRef = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction2<Foo2<*>, CapturedType(*), CapturedType(*)>")!>Foo2<*>::setX1<!>
    konst fooSetRef2 = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction2<Foo2<*>, kotlin.Nothing, kotlin.Any?>")!>id(
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction2<Foo2<*>, CapturedType(*), CapturedType(*)>")!>Foo2<*>::setX1<!>
    )<!>
    konst foo = Foo2<Int>(1)

    fooSetRef.invoke(foo, <!ARGUMENT_TYPE_MISMATCH!>""<!>)
    fooSetRef2.invoke(foo, <!ARGUMENT_TYPE_MISMATCH!>""<!>)

    foo.x.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>bar<!>()
}
