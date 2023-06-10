// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

class BoxT<T>(konst boxed: T)
class BoxT2<T: Any>(konst boxed: T?)
class BoxAny(konst boxed: Any?)
class BoxFoo(konst boxed: IFoo?)

interface IFoo

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class I32<T: Comparable<Int>>(konst konstue: T?) : IFoo where T: Int

fun <T: Comparable<Int>> boxToTypeParameter(x: I32<T>?) where T: Int = BoxT(x)
fun <T: Comparable<Int>> boxToTypeParameter2(x: I32<T>?) where T: Int = BoxT2(x)
fun <T: Comparable<Int>> boxToNullableAny(x: I32<T>?) where T: Int = BoxAny(x)
fun <T: Comparable<Int>> boxToNullableInterface(x: I32<T>?) where T: Int = BoxFoo(x)

fun <T: Comparable<Int>> useNullableI32(x: I32<T>?) where T: Int {
    if (x != null) throw AssertionError()
}

fun box(): String {
    useNullableI32(boxToTypeParameter2<Int>(null).boxed)
    useNullableI32(boxToNullableAny<Int>(null).boxed as I32<Int>?)
    useNullableI32(boxToNullableInterface<Int>(null).boxed as I32<Int>?)

    return "OK"
}