// ISSUE: KT-58260

interface Box<T>

interface Res<E>

operator fun <F> Res<F>.invoke(f: F): F = TODO()

konst <X> Box<in X>.foo: Res<X> get() = TODO()

fun foo(p: Box<in Any?>) {
    p.foo("").length
    p.foo.invoke("").<!UNRESOLVED_REFERENCE!>length<!>

    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String")!>p.foo("")<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any?")!>p.foo.invoke("")<!>
}
