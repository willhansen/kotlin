// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

class BoxT<T>(konst boxed: T)
class BoxAny(konst boxed: Any?)
class BoxFoo(konst boxed: IFoo?)

interface IFoo

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class I32(konst konstue: Int?) : IFoo

fun boxToTypeParameter(x: I32?) = BoxT(x)
fun boxToNullableAny(x: I32?) = BoxAny(x)
fun boxToNullableInterface(x: I32?) = BoxFoo(x)

fun useNullableI32(x: I32?) {
    if (x != null) throw AssertionError()
}

fun box(): String {
    useNullableI32(boxToTypeParameter(null).boxed)
    useNullableI32(boxToNullableAny(null).boxed as I32?)
    useNullableI32(boxToNullableInterface(null).boxed as I32?)

    return "OK"
}