// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

class BoxT<T>(konst boxed: T)
class BoxAny(konst boxed: Any?)
class BoxFoo(konst boxed: IFoo?)

interface IFoo

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class I32<T: Int>(konst konstue: T?) : IFoo where T: Comparable<Int>

fun <T: Int> boxToTypeParameter(x: I32<T>?) where T: Comparable<Int> = BoxT(x)
fun <T: Int> boxToNullableAny(x: I32<T>?) where T: Comparable<Int> = BoxAny(x)
fun <T: Int> boxToNullableInterface(x: I32<T>?) where T: Comparable<Int> = BoxFoo(x)

fun <T: Int> useNullableI32(x: I32<T>?) where T: Comparable<Int> {
    if (x != null) throw AssertionError()
}

fun box(): String {
    useNullableI32(boxToTypeParameter<Int>(null).boxed)
    useNullableI32(boxToNullableAny<Int>(null).boxed as I32<Int>?)
    useNullableI32(boxToNullableInterface<Int>(null).boxed as I32<Int>?)

    return "OK"
}